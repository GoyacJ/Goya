package com.ysmjjsy.goya.component.framework.core.enums.dict;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import com.ysmjjsy.goya.component.framework.common.enums.EnumOption;
import com.ysmjjsy.goya.component.framework.core.i18n.I18nResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>默认枚举字典服务实现</p>
 *
 * <h2>文案解析优先级</h2>
 * <ol>
 *   <li>i18nKey 非空：使用 MessageSource 按 locale 解析</li>
 *   <li>否则使用 label()</li>
 *   <li>否则使用 enum.name()</li>
 * </ol>
 *
 * <h2>缓存</h2>
 * <p>按 (enumFullName, localeTag) 缓存 options 列表。</p>
 *
 * @author goya
 * @since 2026/1/24 16:02
 */
@RequiredArgsConstructor
public class DefaultEnumDictionaryService implements EnumDictionaryService {

    private final EnumDictionaryRegistry registry;
    private final I18nResolver i18nResolver;

    /**
     * key = enumFullName + "@" + localeTag
     */
    private final Map<String, List<EnumOption>> cache = new ConcurrentHashMap<>();

    /** {@inheritDoc} */
    @Override
    public Map<String, String> listEnums() {
        return registry.listExportableEnums();
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, List<String>> conflicts() {
        return registry.conflicts();
    }

    /** {@inheritDoc} */
    @Override
    public List<EnumOption> options(String enumName) {
        Class<? extends Enum<?>> enumClass = registry.resolve(enumName);
        return cache.computeIfAbsent(enumName, k -> buildOptions(enumClass));
    }

    /**
     * 构建 options。
     *
     * @param enumClass 枚举类型
     * @return options
     */
    @SuppressWarnings({"rawtypes"})
    private List<EnumOption> buildOptions(Class<? extends Enum<?>> enumClass) {
        if (!CodeEnum.class.isAssignableFrom(enumClass)) {
            return List.of();
        }

        Enum<?>[] constants = enumClass.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return List.of();
        }

        List<EnumOption> list = new ArrayList<>(constants.length);
        for (Enum<?> c : constants) {
            CodeEnum ce = (CodeEnum) c;

            Serializable code = ce.code();
            String label = resolveLabel(ce, c.name());

            list.add(EnumOption.of(code, label));
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * 解析 label（支持 i18n）。
     *
     * @param e CodeEnum
     * @param fallbackName name()
     * @return label
     */
    private String resolveLabel(CodeEnum<?> e, String fallbackName) {
        String key = e.i18nKey();
        if (StringUtils.hasText(key)) {
            String msg = i18nResolver.getI18nMessage(key);
            if (StringUtils.hasText(msg)) {
                return msg;
            }
        }

        if (StringUtils.hasText(e.label())) {
            return e.label();
        }

        return fallbackName;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, List<EnumOption>> allOptions() {
        Map<String, String> exportable = registry.listExportableEnums();
        Map<String, List<EnumOption>> out = new TreeMap<>();

        for (Map.Entry<String, String> e : exportable.entrySet()) {
            String simpleName = e.getKey();
            String fullName = e.getValue();

            // 使用 fullName 作为解析入口，确保稳定
            List<EnumOption> options = options(fullName);
            out.put(simpleName, options);
        }

        return Collections.unmodifiableMap(out);
    }
}
