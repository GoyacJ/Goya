package com.ysmjjsy.goya.component.framework.servlet.i18n;

import com.ysmjjsy.goya.component.framework.core.autoconfigure.properties.I18nProperties;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.ServletI18nProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import java.util.*;

/**
 * <p>Goya 自定义 LocaleResolver</p>
 *
 * @author goya
 * @since 2026/1/24 15:27
 */
public class GoyaLocaleResolver implements LocaleResolver {

    private final ServletI18nProperties servletProps;
    private final I18nProperties coreProps;
    private final Set<String> supportedTags;

    /**
     * 构造 LocaleResolver。
     *
     * @param servletProps servlet i18n 配置
     * @param coreProps core i18n 配置（用于默认 locale）
     */
    public GoyaLocaleResolver(ServletI18nProperties servletProps, I18nProperties coreProps) {
        this.servletProps = Objects.requireNonNull(servletProps, "servletProps 不能为空");
        this.coreProps = Objects.requireNonNull(coreProps, "coreProps 不能为空");
        this.supportedTags = normalizeSupported(servletProps.supported());
    }

    /** {@inheritDoc} */
    @Override
    @NullMarked
    public Locale resolveLocale(HttpServletRequest request) {
        Locale candidate = null;

        // 1) param
        String p = request.getParameter(servletProps.langParam());
        if (StringUtils.hasText(p)) {
            candidate = parseLocaleTag(p);
        }

        // 2) header
        if (candidate == null) {
            String h = request.getHeader(servletProps.langHeader());
            if (StringUtils.hasText(h)) {
                candidate = parseLocaleTag(h);
            }
        }

        // 3) Accept-Language
        if (candidate == null) {
            candidate = request.getLocale();
        }

        // 4) default
        if (candidate == null) {
            candidate = coreProps.defaultLocale();
        }

        // supported 校验
        if (!supportedTags.isEmpty()) {
            String tag = candidate.toLanguageTag();
            if (!supportedTags.contains(tag)) {
                return coreProps.defaultLocale();
            }
        }

        return candidate;
    }

    /**
     * 不支持 setLocale（无状态策略），如需持久化到 Cookie 可扩展实现。
     */
    @Override
    public void setLocale(@Nullable HttpServletRequest request, HttpServletResponse response, Locale locale) {
        // 无状态策略：不写 Cookie、不写 Session
        // 如需要可扩展：CookieLocaleResolver
    }

    /**
     * 解析 locale tag，例如 zh-CN / en-US。
     *
     * @param tag tag
     * @return Locale 或 null
     */
    private Locale parseLocaleTag(String tag) {
        try {
            Locale l = Locale.forLanguageTag(tag.trim());
            // forLanguageTag 对非法 tag 可能返回 ROOT，这里做一次过滤
            if (l == null || Locale.ROOT.equals(l)) {
                return null;
            }
            return l;
        } catch (Exception _) {
            return null;
        }
    }

    /**
     * 将 supported 列表归一化为语言标签集合。
     *
     * @param supported supported
     * @return set
     */
    private Set<String> normalizeSupported(List<String> supported) {
        if (supported == null || supported.isEmpty()) {
            return Set.of();
        }
        Set<String> out = new HashSet<>();
        for (String s : supported) {
            if (StringUtils.hasText(s)) {
                out.add(Locale.forLanguageTag(s.trim()).toLanguageTag());
            }
        }
        return Collections.unmodifiableSet(out);
    }
}
