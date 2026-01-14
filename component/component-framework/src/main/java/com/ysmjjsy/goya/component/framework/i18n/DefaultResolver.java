package com.ysmjjsy.goya.component.framework.i18n;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/8 23:04
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultResolver implements I18nResolver {

    private final MessageSource messageSource;

    @Override
    public String getI18nMessage(String key) {
        return "";
    }
}
