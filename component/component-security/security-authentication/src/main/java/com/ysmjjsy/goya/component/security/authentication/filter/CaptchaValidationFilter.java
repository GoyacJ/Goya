package com.ysmjjsy.goya.component.security.authentication.filter;

import com.ysmjjsy.goya.component.captcha.api.ICaptchaService;
import com.ysmjjsy.goya.component.captcha.definition.Verification;
import com.ysmjjsy.goya.component.captcha.exception.*;
import com.ysmjjsy.goya.security.authentication.captcha.LoginCaptchaStrategy;
import com.ysmjjsy.goya.security.authentication.exception.*;
import com.ysmjjsy.goya.security.authentication.utils.SecurityRequestUtils;
import com.ysmjjsy.goya.security.authentication.utils.VerificationAssembler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/4 11:34
 */
@Slf4j
@RequiredArgsConstructor
public class CaptchaValidationFilter extends OncePerRequestFilter {

    private final LoginCaptchaStrategy loginCaptchaStrategy;
    private final ICaptchaService iCaptchaService;

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            com.ysmjjsy.goya.security.core.enums.LoginTypeEnum grantType = com.ysmjjsy.goya.security.core.enums.LoginTypeEnum.resolve(request);
            if (grantType == null) {
                SecurityRequestUtils.throwError(
                        OAuth2ErrorCodes.INVALID_REQUEST,
                        "不支持的登录类型"
                );
            }

            String tenantId = request.getParameter("tenant_id");
            if (!loginCaptchaStrategy.shouldValidate(request, grantType, tenantId)) {
                filterChain.doFilter(request, response);
                return;
            }

            Verification verification = VerificationAssembler.from(request, grantType);

            // 校验失败直接抛异常，成功即放行
            if (iCaptchaService.verify(verification)) {
                filterChain.doFilter(request, response);
            }
        } catch (CaptchaException e) {
            switch (e) {
                case CaptchaCategoryIsIncorrectException ex -> throw new SecurityCaptchaException(ex);
                case CaptchaHandlerNotExistException ex -> throw new SecurityCaptchaException(ex);
                case CaptchaHasExpiredException ex -> throw new SecurityCaptchaHasExpiredException(ex);
                case CaptchaIsEmptyException ex -> throw new SecurityCaptchaIsEmptyException(ex);
                case CaptchaMismatchException ex -> throw new SecurityCaptchaMisMatchException(ex);
                case CaptchaParameterIllegalException ex -> throw new SecurityCaptchaArgumentIllegalException(ex);
                default -> {
                }
            }
        } catch (OAuth2AuthenticationException ex) {
            // 明确的 OAuth2 语义错误，必须透传
            throw ex;
        } catch (IllegalArgumentException ex) {
            // 参数非法，客户端错误
            SecurityRequestUtils.throwError(
                    OAuth2ErrorCodes.INVALID_REQUEST,
                    ex.getMessage()
            );
        } catch (Exception ex) {
            // 真正的系统异常
            log.error("验证码系统异常", ex);
            SecurityRequestUtils.throwError(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "验证码服务异常"
            );
        }
    }
}
