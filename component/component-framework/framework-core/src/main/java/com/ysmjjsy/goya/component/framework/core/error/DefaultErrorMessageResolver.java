package com.ysmjjsy.goya.component.framework.core.error;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Objects;

/**
 * <p>默认错误消息解析器实现，基于 Spring {@link MessageSource} 完成国际化解析</p>
 * <p>Locale 策略默认使用 {@link LocaleContextHolder#getLocale()}。
 * 该能力来自 spring-context，不依赖 spring-web。</p>
 *
 * <p>若无法解析 messageKey，则回退到 {@link ErrorCode#defaultMessage()}。</p>
 *
 * @author goya
 * @since 2026/1/24 13:45
 */
public class DefaultErrorMessageResolver implements ErrorMessageResolver {

    private final MessageSource messageSource;

    /**
     * 构造默认错误消息解析器。
     *
     * @param messageSource MessageSource（不能为空）
     */
    public DefaultErrorMessageResolver(MessageSource messageSource) {
        this.messageSource = Objects.requireNonNull(messageSource, "messageSource 不能为空");
    }

    /** {@inheritDoc} */
    @Override
    public String resolve(GoyaException ex) {
        Objects.requireNonNull(ex, "ex 不能为空");
        if (StringUtils.hasText(ex.userMessage())) {
            return ex.userMessage();
        }
        return resolve(ex.errorCode(), ex.args());
    }

    /** {@inheritDoc} */
    @Override
    public String resolve(ErrorCode code, Object[] args) {
        Objects.requireNonNull(code, "code 不能为空");
        Locale locale = LocaleContextHolder.getLocale();
        try {
            String msg = messageSource.getMessage(code.messageKey(), args, locale);
            if (StringUtils.hasText(msg)) {
                return msg;
            }
        } catch (NoSuchMessageException ignore) {
            // 缺失 i18n 文案时回退 defaultMessage
        }
        return code.defaultMessage();
    }
}
