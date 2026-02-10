package com.ysmjjsy.goya.component.security.core.context;

import com.ysmjjsy.goya.component.framework.core.context.GoyaUser;
import com.ysmjjsy.goya.component.framework.servlet.context.AbstractGoyaContext;
import com.ysmjjsy.goya.component.security.core.configuration.properties.SecurityCoreProperties;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * <p>安全上下文实现</p>
 * <p>提供获取当前用户和租户信息的功能</p>
 *
 * @author goya
 * @since 2025/12/31 11:47
 */
@Slf4j
public class GoyaSecurityContext extends AbstractGoyaContext {
}
