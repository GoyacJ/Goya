package com.ysmjjsy.goya.component.common.i18n;

import com.ysmjjsy.goya.component.common.definition.enums.IBizEnum;
import com.ysmjjsy.goya.component.common.enums.LocaleEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;

/**
 * <p>枚举国际化解析器</p>
 *
 * @author goya
 * @since 2025/12/19 23:14
 */
@Slf4j
public class I18nResolver {

    private static MessageSource messageSource;

    public I18nResolver(MessageSource messageSource) {
        I18nResolver.messageSource = messageSource;
    }


    /**
     * 解析
     *
     * @param e      enum
     * @return message
     */
    public static String resolveEnum(IBizEnum<?> e) {
        return messageSource.getMessage(
                e.getI18nKey(),
                null,
                e.getDescription(),
                LocaleEnum.getDefaultLocal()
        );
    }
}
