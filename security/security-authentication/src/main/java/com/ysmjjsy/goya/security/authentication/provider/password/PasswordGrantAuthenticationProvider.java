package com.ysmjjsy.goya.security.authentication.provider.password;

import com.ysmjjsy.goya.security.authentication.constants.ISecurityAuthenticationConstants;
import com.ysmjjsy.goya.security.authentication.enums.LoginGrantType;
import com.ysmjjsy.goya.security.authentication.provider.AbstractAuthenticationProvider;
import com.ysmjjsy.goya.security.authentication.token.TokenService;
import com.ysmjjsy.goya.security.authentication.utils.DPoPProofVerifier;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.security.core.service.ISecurityUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Set;

/**
 * <p>密码Grant Type认证提供者</p>
 * <p>处理Password Grant Type的认证流程：</p>
 * <ol>
 *   <li>验证验证码（如果提供）</li>
 *   <li>验证用户名和密码</li>
 *   <li>生成OAuth2 Access Token和Refresh Token</li>
 * </ol>
 *
 * <p>这是OAuth2.1的非标准扩展，仅用于企业内部系统</p>
 *
 * @author goya
 * @see PasswordGrantAuthenticationConverter
 * @see PasswordGrantAuthenticationToken
 * @see <a href="https://github.com/spring-projects/spring-authorization-server/blob/main/docs/modules/ROOT/pages/guides/how-to-ext-grant-type.adoc">Spring Authorization Server - Extending Grant Types</a>
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class PasswordGrantAuthenticationProvider extends AbstractAuthenticationProvider {

    private final ISecurityUserService securityUserService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    @NullMarked
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PasswordGrantAuthenticationToken passwordGrantAuthentication = (PasswordGrantAuthenticationToken) authentication;

        // 1. 验证客户端认证
        OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(passwordGrantAuthentication);
        RegisteredClient registeredClient = getRegisteredClient(clientPrincipal);
        // 验证客户端是否支持password grant type
        checkAuthorizedGrantType(registeredClient, LoginGrantType.PASSWORD.getGrantType());

        Jwt dPoPProof = DPoPProofVerifier.verifyIfAvailable(passwordGrantAuthentication);

        String username = (String) passwordGrantAuthentication.getAdditionalParameters().get(ISecurityAuthenticationConstants.USERNAME);
        String password = (String) passwordGrantAuthentication.getAdditionalParameters().get(ISecurityAuthenticationConstants.PASSWORD);

        // 2. 验证用户名和密码
        SecurityUser user = validateUserCredentials(username, password);

        // 3. 确定授权的scope
        Set<String> authorizedScopes = validateScopes(registeredClient, passwordGrantAuthentication.getScopes());

        // 4. 使用TokenService生成Token（包括access token和refresh token）
        var tokenResponse = tokenService.generateToken(
                registeredClient,
                user,
                LoginGrantType.PASSWORD.getGrantType(),
                authorizedScopes,
                passwordGrantAuthentication,
                dPoPProof);

        // 5. 构建OAuth2AccessToken对象
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                tokenResponse.accessToken(),
                tokenResponse.issuedAt(),
                tokenResponse.expiresAt(),
                null);

        // 6. 构建OAuth2RefreshToken（如果存在）
        OAuth2RefreshToken refreshToken = null;
        if (tokenResponse.refreshToken() != null) {
            refreshToken = new OAuth2RefreshToken(
                    tokenResponse.refreshToken(),
                    tokenResponse.issuedAt(),
                    tokenResponse.expiresAt());
        }

        log.debug("[Goya] |- security [authentication] Password grant authentication successful for user: {}", user.getUsername());

        // 7. 返回认证结果
        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient,
                clientPrincipal,
                accessToken,
                refreshToken,
                passwordGrantAuthentication.getAdditionalParameters());
    }

    @Override
    @NullMarked
    public boolean supports(Class<?> authentication) {
        return PasswordGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 验证用户凭证
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     * @throws AuthenticationException 如果用户认证失败
     */
    private SecurityUser validateUserCredentials(String username, String password) {
        try {
            // 查找用户
            SecurityUser user = securityUserService.findUser(username);
            if (user == null) {
                log.warn("[Goya] |- security [authentication] User not found: {}", username);
                throw new BadCredentialsException("用户名或密码错误");
            }

            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("[Goya] |- security [authentication] Password mismatch for user: {}", username);
                throw new BadCredentialsException("用户名或密码错误");
            }

            // 检查账户状态
            if (!user.isEnabled()) {
                throw new BadCredentialsException("账户已被禁用");
            }
            if (!user.isAccountNonLocked()) {
                throw new BadCredentialsException("账户已被锁定");
            }
            if (!user.isAccountNonExpired()) {
                throw new BadCredentialsException("账户已过期");
            }
            if (!user.isCredentialsNonExpired()) {
                throw new BadCredentialsException("凭证已过期");
            }

            return user;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Error validating user credentials for: {}", username, e);
            throw new BadCredentialsException("用户认证失败", e);
        }
    }

}

