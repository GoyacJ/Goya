package com.ysmjjsy.goya.component.security.authentication.provider.login;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.util.List;

/**
 * <p>组合认证转换器</p>
 * <p>按顺序尝试多个AuthenticationConverter，返回第一个非null的结果</p>
 * <p>用于支持多种登录方式（密码、短信、社交）</p>
 * <p>符合Spring Security标准模式</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class CompositeAuthenticationConverter implements AuthenticationConverter {

    private final List<AuthenticationConverter> converters;

    @Override
    public Authentication convert(@NonNull HttpServletRequest request) {
        for (AuthenticationConverter converter : converters) {
            try {
                Authentication authentication = converter.convert(request);
                if (authentication != null) {
                    log.debug("[Goya] |- security [authentication] Authentication converter {} successfully converted request",
                            converter.getClass().getSimpleName());
                    return authentication;
                }
            } catch (Exception e) {
                log.debug("[Goya] |- security [authentication] Authentication converter {} failed: {}",
                        converter.getClass().getSimpleName(), e.getMessage());
                // 继续尝试下一个Converter
            }
        }

        log.debug("[Goya] |- security [authentication] No authentication converter could convert the request");
        return null;
    }
}

