package com.ysmjjsy.goya.component.common.i18n;

import com.ysmjjsy.goya.component.common.definition.enums.IBizEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * <p>枚举国际化解析器</p>
 *
 * @author goya
 * @since 2025/12/19 23:14
 */
@Slf4j
@RequiredArgsConstructor
public class EnumI18nResolver {

    private final MessageSource messageSource;

    /**
     * 解析
     * @param e enum
     * @param locale 语言
     * @return message
     */
    public String resolve(IBizEnum<?> e, Locale locale) {
        return messageSource.getMessage(
                e.getI18nKey(),
                null,
                e.getDescription(),
                locale
        );
    }
}
