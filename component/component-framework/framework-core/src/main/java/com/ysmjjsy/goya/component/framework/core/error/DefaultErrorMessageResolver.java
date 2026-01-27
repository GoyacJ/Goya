package com.ysmjjsy.goya.component.framework.core.error;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.core.i18n.I18nResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

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
@RequiredArgsConstructor
public class DefaultErrorMessageResolver implements ErrorMessageResolver {

    private final I18nResolver i18nResolver;

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve(GoyaException ex) {
        Objects.requireNonNull(ex, "ex 不能为空");
        if (StringUtils.hasText(ex.userMessage())) {
            return ex.userMessage();
        }
        return resolve(ex.errorCode(), ex.args());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve(ErrorCode code, Object[] args) {
        Objects.requireNonNull(code, "code 不能为空");
        try {
            String msg = i18nResolver.getI18nMessage(code.messageKey());
            if (StringUtils.hasText(msg)) {
                return msg;
            }
        } catch (NoSuchMessageException ignore) {
            // 缺失 i18n 文案时回退 defaultMessage
        }
        return code.defaultMessage();
    }
}
