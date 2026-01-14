package com.ysmjjsy.goya.component.framework.i18n;

/**
 * <p>I18nHandler</p>
 *
 * @author goya
 * @since 2026/1/7 23:01
 */
public interface I18nResolver {

    /**
     * <p>get i18n message</p>
     *
     * @param key i18n key
     * @return i18n message
     */
    String getI18nMessage(String key);
}
