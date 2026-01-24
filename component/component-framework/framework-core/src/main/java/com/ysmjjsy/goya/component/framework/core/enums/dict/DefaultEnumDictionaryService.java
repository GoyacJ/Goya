package com.ysmjjsy.goya.component.framework.core.enums.dict;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import com.ysmjjsy.goya.component.framework.common.enums.EnumOption;
import org.springframework.context.MessageSource;
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
public class DefaultEnumDictionaryService implements EnumDictionaryService {

    private final EnumDictionaryRegistry registry;
    private final MessageSource messageSource;
    private final LocaleProvider localeProvider;

    /**
     * key = enumFullName + "@" + localeTag
     */
    private final Map<String, List<EnumOption>> cache = new ConcurrentHashMap<>();

    /**
     * 构造服务。
     *
     * @param registry 注册表
     * @param messageSource MessageSource
     * @param localeProvider LocaleProvider
     */
    public DefaultEnumDictionaryService(EnumDictionaryRegistry registry,
                                        MessageSource messageSource,
                                        LocaleProvider localeProvider) {
        this.registry = Objects.requireNonNull(registry, "registry 不能为空");
        this.messageSource = Objects.requireNonNull(messageSource, "messageSource 不能为空");
        this.localeProvider = Objects.requireNonNull(localeProvider, "localeProvider 不能为空");
    }

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
    public List<EnumOption> options(String enumName, Locale locale) {
        Class<? extends Enum<?>> enumClass = registry.resolve(enumName);
        Locale useLocale = (locale != null) ? locale : localeProvider.currentLocale();

        String key = enumClass.getName() + "@" + useLocale.toLanguageTag();
        return cache.computeIfAbsent(key, k -> buildOptions(enumClass, useLocale));
    }

    /**
     * 构建 options。
     *
     * @param enumClass 枚举类型
     * @param locale locale
     * @return options
     */
    @SuppressWarnings({"rawtypes"})
    private List<EnumOption> buildOptions(Class<? extends Enum<?>> enumClass, Locale locale) {
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
            String label = resolveLabel(ce, c.name(), locale);

            list.add(EnumOption.of(code, label));
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * 解析 label（支持 i18n）。
     *
     * @param e CodeEnum
     * @param fallbackName name()
     * @param locale locale
     * @return label
     */
    private String resolveLabel(CodeEnum<?> e, String fallbackName, Locale locale) {
        String key = e.i18nKey();
        if (StringUtils.hasText(key)) {
            String msg = messageSource.getMessage(key, null, key, locale);
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
    public Map<String, List<EnumOption>> allOptions(Locale locale) {
        Locale useLocale = (locale != null) ? locale : localeProvider.currentLocale();

        Map<String, String> exportable = registry.listExportableEnums();
        Map<String, List<EnumOption>> out = new TreeMap<>();

        for (Map.Entry<String, String> e : exportable.entrySet()) {
            String simpleName = e.getKey();
            String fullName = e.getValue();

            // 使用 fullName 作为解析入口，确保稳定
            List<EnumOption> options = options(fullName, useLocale);
            out.put(simpleName, options);
        }

        return Collections.unmodifiableMap(out);
    }
}
