package com.ysmjjsy.goya.component.framework.log.mask;

import com.ysmjjsy.goya.component.framework.masker.annotation.Sensitive;
import com.ysmjjsy.goya.component.framework.masker.core.Masker;
import com.ysmjjsy.goya.component.framework.masker.core.MaskingRules;
import com.ysmjjsy.goya.component.framework.masker.core.SensitiveType;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>方法参数脱敏助手：实现“参数级 @Sensitive 优先于其他规则”</p>
 * <p>输出结构采用 Map（paramName -> maskedValue），便于日志检索与定位。</p>
 * @author goya
 * @since 2026/1/24 22:21
 */
public class MethodArgMasker {

    private final Masker masker;
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 构造助手。
     *
     * @param masker 脱敏器
     */
    public MethodArgMasker(Masker masker) {
        this.masker = Objects.requireNonNull(masker, "masker 不能为空");
    }

    /**
     * 对方法入参做脱敏。
     *
     * @param method 方法
     * @param args 参数值数组
     * @param enableMask 是否启用脱敏
     * @return map：参数名 -> 脱敏后值
     */
    public Map<String, Object> maskArgs(Method method, Object[] args, boolean enableMask) {
        String[] names = nameDiscoverer.getParameterNames(method);
        Annotation[][] anns = method.getParameterAnnotations();

        int n = (args == null) ? 0 : args.length;
        Map<String, Object> out = LinkedHashMap.newLinkedHashMap(Math.max(8, n));

        for (int i = 0; i < n; i++) {
            String name = (names != null && i < names.length && names[i] != null) ? names[i] : ("arg" + i);
            Object v = args[i];

            Sensitive sensitive = findSensitive(anns, i);
            if (!enableMask) {
                out.put(name, v);
                continue;
            }

            if (sensitive != null) {
                out.put(name, applySensitive(sensitive.type(), v));
            } else {
                out.put(name, masker.mask(v));
            }
        }

        return out;
    }

    private Sensitive findSensitive(Annotation[][] anns, int idx) {
        if (anns == null || idx < 0 || idx >= anns.length) {
            return null;
        }
        for (Annotation a : anns[idx]) {
            if (a instanceof Sensitive s) {
                return s;
            }
        }
        return null;
    }

    private Object applySensitive(SensitiveType type, Object val) {
        String s = (val == null) ? null : String.valueOf(val);
        return switch (type) {
            case PASSWORD -> MaskingRules.password(s);
            case TOKEN, HEADER_CREDENTIAL -> MaskingRules.token(s);
            case EMAIL -> MaskingRules.email(s);
            case PHONE -> MaskingRules.phone(s);
            case ID_CARD, BANK_CARD, GENERIC -> MaskingRules.generic(s);
        };
    }
}
