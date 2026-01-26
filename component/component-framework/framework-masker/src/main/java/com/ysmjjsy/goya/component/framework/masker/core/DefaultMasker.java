package com.ysmjjsy.goya.component.framework.masker.core;

import com.ysmjjsy.goya.component.framework.masker.annotation.Sensitive;
import com.ysmjjsy.goya.component.framework.masker.autoconfigure.properties.MaskerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>默认脱敏器实现。</p>
 *
 * <p>支持对：</p>
 * <ul>
 *   <li>Map：按 key 分类脱敏，并递归处理 value</li>
 *   <li>Iterable/数组：递归处理元素</li>
 *   <li>CharSequence：按规则脱敏与截断</li>
 *   <li>其他对象：使用 toString 并截断（不做反射展开，避免性能与隐私风险）</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/24 22:02
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultMasker implements Masker {

    private final MaskerProperties props;
    private final MaskingKeyClassifier classifier;

    private static final Map<Class<?>, BeanMeta> BEAN_META_CACHE = new ConcurrentHashMap<>();

    @Override
    public Object mask(Object input) {
        return mask(input, MaskingMode.LOG);
    }

    @Override
    public Object mask(Object input, MaskingMode mode) {
        MaskingMode useMode = (mode == null) ? MaskingMode.LOG : mode;
        if (!props.enabled()) {
            return safeToString(input);
        }
        return mask0(input, 0, new IdentityHashMap<>(), useMode);
    }

    private Object mask0(Object input, int depth, IdentityHashMap<Object, Boolean> visiting, MaskingMode mode) {
        if (input == null) {
            return null;
        }
        if (depth > props.maxDepth()) {
            return "[MAX_DEPTH]";
        }

        if (needsCycleGuard(input) && visiting.put(input, Boolean.TRUE) != null) {
            return "[CYCLE]";
        }

        try {
            switch (input) {
                case CharSequence cs -> {
                    return truncate(cs.toString());
                }
                case Map<?, ?> map -> {
                    Map<String, Object> out = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> e : map.entrySet()) {
                        String key = String.valueOf(e.getKey());
                        Object val = e.getValue();

                        SensitiveType type = classifier.classify(key);
                        if (type != null) {
                            out.put(key, applyRule(type, val));
                        } else {
                            out.put(key, mask0(val, depth + 1, visiting, mode));
                        }
                    }
                    return out;
                }
                case Iterable<?> it -> {
                    List<Object> out = new ArrayList<>();
                    for (Object v : it) {
                        out.add(mask0(v, depth + 1, visiting, mode));
                    }
                    return out;
                }
                default -> {
                }
            }

            Class<?> cls = input.getClass();
            if (cls.isArray()) {
                int len = Array.getLength(input);
                List<Object> out = new ArrayList<>(len);
                for (int i = 0; i < len; i++) {
                    out.add(mask0(Array.get(input, i), depth + 1, visiting, mode));
                }
                return out;
            }

            if (isSimpleValue(input)) {
                return input;
            }

            // record：可安全展开（不会改变“对象还是对象”的事实；但你当前实现返回 Map，如果你想严格保持 record 类型，后续可再做 record 构造器拷贝）
            if (cls.isRecord() && props.expandBeanOnSensitive() && hasSensitiveMember(cls)) {
                // 这里为了兼容既有行为，继续返回 Map（日志友好）
                // API 场景如果你要求 record 类型不变，我可以再加“按 canonical ctor 重建 record”的实现
                return maskRecordToMap(input, depth + 1, visiting, mode);
            }

            // 普通 POJO：只在存在 @Sensitive 时处理
            if (props.expandBeanOnSensitive() && hasSensitiveMember(cls)) {
                // API：优先“同类型拷贝式脱敏”
                if (mode == MaskingMode.API && props.beanCopyEnabled()) {
                    Object copied = tryBeanCopyMasking(input, depth + 1, visiting, mode);
                    if (copied != null) {
                        return copied;
                    }
                    // API 模式拷贝失败：不改变类型，不退化为 Map
                    return input;
                }

                // LOG：尽量不泄漏，拷贝失败可退化为 Map
                if (props.beanCopyEnabled()) {
                    Object copied = tryBeanCopyMasking(input, depth + 1, visiting, mode);
                    if (copied != null) {
                        return copied;
                    }
                }
                return maskBeanToMap(input, depth + 1, visiting, mode);
            }

            // 默认：不展开对象图
            return safeToString(input);
        } finally {
            if (needsCycleGuard(input)) {
                visiting.remove(input);
            }
        }
    }

    private boolean isSimpleValue(Object v) {
        return v instanceof Number || v instanceof Boolean || v instanceof Enum<?> || v instanceof UUID;
    }

    private boolean needsCycleGuard(Object input) {
        return !(input instanceof CharSequence) && !isSimpleValue(input);
    }

    private Object applyRule(SensitiveType type, Object val) {
        String s = (val == null) ? null : String.valueOf(val);
        return switch (type) {
            case PASSWORD -> MaskingRules.password(s);
            case TOKEN, HEADER_CREDENTIAL -> MaskingRules.token(s);
            case EMAIL -> MaskingRules.email(s);
            case PHONE -> MaskingRules.phone(s);
            case ID_CARD, BANK_CARD, GENERIC -> MaskingRules.generic(s);
        };
    }

    private boolean hasSensitiveMember(Class<?> cls) {
        if (cls == null) {
            return false;
        }
        if (cls.isRecord()) {
            for (RecordComponent rc : cls.getRecordComponents()) {
                if (rc.getAnnotation(Sensitive.class) != null) {
                    return true;
                }
            }
            return false;
        }
        for (Field f : getAllFields(cls)) {
            if (f.getAnnotation(Sensitive.class) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * API 关键：同类型拷贝式脱敏。
     *
     * <p>返回 null 表示拷贝失败。</p>
     */
    private Object tryBeanCopyMasking(Object bean, int depth, IdentityHashMap<Object, Boolean> visiting, MaskingMode mode) {
        Class<?> cls = bean.getClass();
        BeanMeta meta = BEAN_META_CACHE.computeIfAbsent(cls, this::buildBeanMeta);
        if (!meta.copyable) {
            return null;
        }
        Object target = meta.newInstance();
        if (target == null) {
            return null;
        }

        int limit = Math.max(1, props.maxBeanFields());
        int count = 0;

        // 走属性（getter/setter）优先
        for (PropertyDescriptor pd : meta.props) {
            if (count++ >= limit) {
                // 字段过多：直接停止（保持性能）
                break;
            }
            String name = pd.getName();
            Method read = pd.getReadMethod();
            Method write = pd.getWriteMethod();

            if (read == null) {
                continue;
            }
            Object value = invoke(read, bean);

            Sensitive ann = meta.sensitiveByName.get(name);
            Object outVal = (ann != null)
                    ? applyRule(ann.type(), value)
                    : mask0(value, depth, visiting, mode);

            boolean wrote = false;
            if (write != null) {
                wrote = invokeSetter(write, target, outVal);
            }

            // 没有 setter 时是否允许字段写
            if (!wrote && props.beanCopyUseFieldAccess()) {
                Field f = meta.fieldByName.get(name);
                if (f != null) {
                    setField(f, target, outVal);
                }
            }
        }

        return target;
    }

    private BeanMeta buildBeanMeta(Class<?> cls) {
        // record 不走这里
        if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers()) || cls.isEnum() || cls.isArray()) {
            return BeanMeta.notCopyable();
        }
        Constructor<?> ctor = findNoArgConstructor(cls);
        if (ctor == null) {
            return BeanMeta.notCopyable();
        }

        List<PropertyDescriptor> pds = new ArrayList<>();
        Map<String, Field> fieldByName = new HashMap<>();
        Map<String, Sensitive> sensitiveByName = new HashMap<>();

        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(cls, Object.class).getPropertyDescriptors()) {
                if (pd == null) {
                    continue;
                }
                String name = pd.getName();
                if (name == null || name.isBlank()) {
                    continue;
                }
                // 过滤 class
                if ("class".equals(name)) {
                    continue;
                }
                pds.add(pd);
            }
        } catch (Exception _) {
            return BeanMeta.notCopyable();
        }

        for (Field f : getAllFields(cls)) {
            if (f == null || Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            f.setAccessible(true);
            fieldByName.putIfAbsent(f.getName(), f);
            Sensitive ann = f.getAnnotation(Sensitive.class);
            if (ann != null) {
                sensitiveByName.put(f.getName(), ann);
            }
        }

        // 也允许 getter 上标注 @Sensitive（更贴近“属性语义”）
        for (PropertyDescriptor pd : pds) {
            Method read = pd.getReadMethod();
            if (read != null) {
                Sensitive ann = read.getAnnotation(Sensitive.class);
                if (ann != null) {
                    sensitiveByName.put(pd.getName(), ann);
                }
            }
        }

        return new BeanMeta(ctor, Collections.unmodifiableList(pds),
                Collections.unmodifiableMap(fieldByName),
                Collections.unmodifiableMap(sensitiveByName));
    }

    private Constructor<?> findNoArgConstructor(Class<?> cls) {
        try {
            Constructor<?> c = cls.getDeclaredConstructor();
            c.setAccessible(true);
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    private Object invoke(Method m, Object target) {
        try {
            m.setAccessible(true);
            return m.invoke(target);
        } catch (Exception e) {
            return "[INACCESSIBLE]";
        }
    }

    private boolean invokeSetter(Method write, Object target, Object value) {
        try {
            write.setAccessible(true);
            Class<?> pt = write.getParameterTypes()[0];
            Object v = adaptValue(value, pt);
            write.invoke(target, v);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setField(Field f, Object target, Object value) {
        try {
            f.setAccessible(true);
            Object v = adaptValue(value, f.getType());
            f.set(target, v);
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    /**
     * 简单类型适配：当字段类型不是 Object，但 value 是 Map/List 等时避免强制写入导致异常。
     *
     * <p>策略：类型不兼容就跳过写入（返回 null 让 setter/field 写入失败）。</p>
     */
    private Object adaptValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        // 基本类型自动装箱兼容
        if (targetType.isPrimitive()) {
            Class<?> boxed = boxType(targetType);
            if (boxed != null && boxed.isInstance(value)) {
                return value;
            }
        }
        // 类型不兼容：返回一个特殊标记，触发写入失败
        return null;
    }

    private Class<?> boxType(Class<?> primitive) {
        if (primitive == boolean.class) {
            return Boolean.class;
        }
        if (primitive == int.class) {
            return Integer.class;
        }
        if (primitive == long.class) {
            return Long.class;
        }
        if (primitive == double.class) {
            return Double.class;
        }
        if (primitive == float.class) {
            return Float.class;
        }
        if (primitive == short.class) {
            return Short.class;
        }
        if (primitive == byte.class) {
            return Byte.class;
        }
        if (primitive == char.class) {
            return Character.class;
        }
        return null;
    }

    private Map<String, Object> maskBeanToMap(Object bean, int depth, IdentityHashMap<Object, Boolean> visiting, MaskingMode mode) {
        Class<?> cls = bean.getClass();
        Map<String, Object> out = new LinkedHashMap<>();

        int limit = Math.max(1, props.maxBeanFields());
        int count = 0;

        for (Field f : getAllFields(cls)) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            if (count++ >= limit) {
                out.put("_truncated", true);
                break;
            }
            f.setAccessible(true);
            String name = f.getName();
            Object v = readField(bean, f);
            Sensitive ann = f.getAnnotation(Sensitive.class);

            if (ann != null) {
                out.put(name, applyRule(ann.type(), v));
            } else {
                out.put(name, mask0(v, depth, visiting, mode));
            }
        }
        return out;
    }

    private Map<String, Object> maskRecordToMap(Object record, int depth, IdentityHashMap<Object, Boolean> visiting, MaskingMode mode) {
        Map<String, Object> out = new LinkedHashMap<>();
        Class<?> cls = record.getClass();

        int limit = Math.max(1, props.maxBeanFields());
        int count = 0;

        for (RecordComponent rc : cls.getRecordComponents()) {
            if (count++ >= limit) {
                out.put("_truncated", true);
                break;
            }
            String name = rc.getName();
            Sensitive ann = rc.getAnnotation(Sensitive.class);
            Object v = invokeRecordAccessor(record, rc);

            if (ann != null) {
                out.put(name, applyRule(ann.type(), v));
            } else {
                out.put(name, mask0(v, depth, visiting, mode));
            }
        }
        return out;
    }

    private Object invokeRecordAccessor(Object bean, RecordComponent rc) {
        try {
            Method m = rc.getAccessor();
            m.setAccessible(true);
            return m.invoke(bean);
        } catch (Exception _) {
            return "[INACCESSIBLE]";
        }
    }

    private Object readField(Object bean, Field f) {
        try {
            return f.get(bean);
        } catch (Exception _) {
            return "[INACCESSIBLE]";
        }
    }

    private List<Field> getAllFields(Class<?> cls) {
        List<Field> list = new ArrayList<>();
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            Collections.addAll(list, c.getDeclaredFields());
            c = c.getSuperclass();
        }
        return list;
    }

    private String safeToString(Object v) {
        return v == null ? null : truncate(String.valueOf(v));
    }

    private String truncate(String s) {
        if (s == null) {
            return null;
        }
        int max = props.maxStringLength();
        if (max <= 0 || s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...(truncated)";
    }

    /**
     * JavaBean 元信息缓存。
     */
    private static final class BeanMeta {

        private final Constructor<?> ctor;
        private final List<PropertyDescriptor> props;
        private final Map<String, Field> fieldByName;
        private final Map<String, Sensitive> sensitiveByName;
        private final boolean copyable;

        private BeanMeta(Constructor<?> ctor,
                         List<PropertyDescriptor> props,
                         Map<String, Field> fieldByName,
                         Map<String, Sensitive> sensitiveByName) {
            this.ctor = ctor;
            this.props = props;
            this.fieldByName = fieldByName;
            this.sensitiveByName = sensitiveByName;
            this.copyable = true;
        }

        static BeanMeta notCopyable() {
            return new BeanMeta(null, List.of(), Map.of(), Map.of(), false);
        }

        private BeanMeta(Constructor<?> ctor,
                         List<PropertyDescriptor> props,
                         Map<String, Field> fieldByName,
                         Map<String, Sensitive> sensitiveByName,
                         boolean copyable) {
            this.ctor = ctor;
            this.props = props;
            this.fieldByName = fieldByName;
            this.sensitiveByName = sensitiveByName;
            this.copyable = copyable;
        }

        Object newInstance() {
            if (!copyable || ctor == null) {
                return null;
            }
            try {
                ctor.setAccessible(true);
                return ctor.newInstance();
            } catch (Exception _) {
                return null;
            }
        }
    }
}
