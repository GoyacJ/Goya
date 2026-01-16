package com.ysmjjsy.goya.component.framework.i18n;

import com.ysmjjsy.goya.component.core.enums.IBizEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/8 23:04
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultResolver implements I18nResolver, ApplicationContextAware {

    @Getter
    private static MessageSource messageSource;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        messageSource = applicationContext.getBean(MessageSource.class);
    }

    /**
     * 解析枚举的国际化消息（使用默认 Locale）
     * <p>
     * Locale 获取优先级：
     * <ol>
     *   <li>请求级别的 Locale（LocaleContextHolder.getLocale()）</li>
     *   <li>系统配置的默认 Locale（PlatformProperties.locale）</li>
     *   <li>JVM 默认 Locale（Locale.getDefault()）</li>
     * </ol>
     *
     * @param enumValue 枚举值，必须实现 IBizEnum 接口
     * @return 国际化消息，如果解析失败则返回枚举的默认描述
     * @throws IllegalArgumentException 如果 enumValue 为 null
     */
    public static String resolveEnum(IBizEnum<?> enumValue) {
        if (enumValue == null) {
            throw new IllegalArgumentException("枚举值不能为 null");
        }

        // 获取 Locale：优先使用请求级别的 Locale
        Locale locale = getLocale();

        return resolveEnum(enumValue, locale);
    }

    /**
     * 解析枚举的国际化消息（指定 Locale）
     *
     * @param enumValue 枚举值，必须实现 IBizEnum 接口
     * @param locale    指定的 Locale，如果为 null 则使用默认 Locale
     * @return 国际化消息，如果解析失败则返回枚举的默认描述
     * @throws IllegalArgumentException 如果 enumValue 为 null
     */
    public static String resolveEnum(IBizEnum<?> enumValue, Locale locale) {
        if (enumValue == null) {
            throw new IllegalArgumentException("枚举值不能为 null");
        }

        // 如果 MessageSource 未初始化，返回默认描述
        if (messageSource == null) {
            log.warn("[Goya] |- MessageSource 未初始化，返回枚举默认描述: {}", enumValue.getDescription());
            return enumValue.getDescription();
        }

        // 如果 Locale 为 null，使用默认 Locale
        if (locale == null) {
            locale = getLocale();
        }

        String i18nKey = enumValue.getI18nKey();
        String defaultMessage = enumValue.getDescription();

        try {
            // 尝试获取国际化消息
            String message = messageSource.getMessage(
                    i18nKey,
                    null, // 参数数组，可以为 null
                    defaultMessage, // 默认消息（降级策略）
                    locale
            );

            // 如果返回的是 key 本身（表示未找到），使用默认描述
            if (StringUtils.equals(message, i18nKey)) {
                log.debug("[Goya] |- 国际化消息未找到，使用默认描述: key={}, locale={}", i18nKey, locale);
                return defaultMessage;
            }

            return message;
        } catch (Exception e) {
            // 异常降级：返回默认描述
            log.warn("[Goya] |- 获取国际化消息失败，使用默认描述: key={}, locale={}, error={}",
                    i18nKey, locale, e.getMessage());
            return defaultMessage;
        }
    }

    /**
     * 解析枚举的国际化消息（使用消息键和参数）
     * <p>
     * 支持带参数的国际化消息，例如：{@code "user.not.found" -> "用户 {0} 不存在"}
     *
     * @param enumValue 枚举值
     * @param args      消息参数
     * @return 国际化消息
     */
    public static String resolveEnum(IBizEnum<?> enumValue, Object... args) {
        if (enumValue == null) {
            throw new IllegalArgumentException("枚举值不能为 null");
        }

        Locale locale = getLocale();

        // 如果 MessageSource 未初始化，返回默认描述
        if (messageSource == null) {
            log.warn("[Goya] |- MessageSource 未初始化，返回枚举默认描述: {}", enumValue.getDescription());
            return enumValue.getDescription();
        }

        String i18nKey = enumValue.getI18nKey();
        String defaultMessage = enumValue.getDescription();

        try {
            String message = messageSource.getMessage(
                    i18nKey,
                    args,
                    defaultMessage,
                    locale
            );

            // 如果返回的是 key 本身，使用默认描述
            if (StringUtils.equals(message, i18nKey)) {
                log.debug("[Goya] |- 国际化消息未找到，使用默认描述: key={}, locale={}", i18nKey, locale);
                return defaultMessage;
            }

            return message;
        } catch (Exception e) {
            log.warn("[Goya] |- 获取国际化消息失败，使用默认描述: key={}, locale={}, error={}",
                    i18nKey, locale, e.getMessage());
            return defaultMessage;
        }
    }

    /**
     * 获取当前 Locale
     * <p>
     * Locale 获取优先级：
     * <ol>
     *   <li>请求级别的 Locale（LocaleContextHolder.getLocale()）</li>
     *   <li>系统配置的默认 Locale（PlatformProperties.locale）</li>
     *   <li>JVM 默认 Locale（Locale.getDefault()）</li>
     * </ol>
     *
     * @return 当前 Locale，不会返回 null
     */
    private static Locale getLocale() {

        try {
            // 2. 使优先用系统配置的默认 Locale
            Locale locale = LocaleEnum.getDefaultLocal();
            if (locale != null) {
                return locale;
            }
        } catch (Exception e) {
            log.debug("[Goya] |- 获取系统默认 Locale 失败: {}", e.getMessage());
        }

        try {
            // 1. 使用请求级别的 Locale（从 HTTP 请求头获取）
            Locale locale = LocaleContextHolder.getLocale();
            if (locale != null) {
                return locale;
            }
        } catch (Exception e) {
            log.debug("[Goya] |- 获取请求级别 Locale 失败: {}", e.getMessage());
        }

        // 3. 使用 JVM 默认 Locale
        return Locale.getDefault();
    }

    @Override
    public String getI18nMessage(String key) {
        return messageSource.getMessage(key, null, getLocale());
    }
}
