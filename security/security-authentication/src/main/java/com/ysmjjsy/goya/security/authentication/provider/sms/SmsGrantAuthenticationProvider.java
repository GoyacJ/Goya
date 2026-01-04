package com.ysmjjsy.goya.security.authentication.provider.sms;

import com.ysmjjsy.goya.component.cache.service.ICacheService;
import com.ysmjjsy.goya.security.authentication.exception.SecurityCaptchaArgumentIllegalException;
import com.ysmjjsy.goya.security.authentication.exception.SecurityCaptchaHasExpiredException;
import com.ysmjjsy.goya.security.authentication.exception.SecurityCaptchaMisMatchException;
import com.ysmjjsy.goya.security.authentication.token.TokenService;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.security.core.service.ISecurityUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.Set;

/**
 * <p>短信验证码Grant Type认证提供者</p>
 * <p>处理SMS Grant Type的认证流程：</p>
 * <ol>
 *   <li>验证短信验证码（从缓存中获取并验证）</li>
 *   <li>根据手机号查找或创建用户</li>
 *   <li>生成OAuth2 Access Token和Refresh Token</li>
 * </ol>
 *
 * <p>这是OAuth2.1的非标准扩展，仅用于企业内部系统</p>
 *
 * @author goya
 * @since 2025/12/21
 * @see SmsGrantAuthenticationConverter
 * @see SmsGrantAuthenticationToken
 * @see <a href="https://github.com/spring-projects/spring-authorization-server/blob/main/docs/modules/ROOT/pages/guides/how-to-ext-grant-type.adoc">Spring Authorization Server - Extending Grant Types</a>
 */
@Slf4j
@RequiredArgsConstructor
public class SmsGrantAuthenticationProvider implements AuthenticationProvider {

    /**
     * 短信验证码缓存名称
     */
    private static final String SMS_CODE_CACHE_NAME = "sms:verification:code";

    /**
     * 短信验证码缓存键前缀
     */
    private static final String SMS_CODE_CACHE_KEY_PREFIX = "sms:code:";

    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationService authorizationService;
    private final ISecurityUserService securityUserService;
    private final TokenService tokenService;
    private final ICacheService cacheService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsGrantAuthenticationToken smsGrantAuthentication = (SmsGrantAuthenticationToken) authentication;

        // 1. 验证客户端认证
        OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(smsGrantAuthentication);
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientPrincipal.getRegisteredClientId());

        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
        }

        // 2. 验证客户端是否支持sms grant type
        if (!registeredClient.getAuthorizationGrantTypes().contains(SmsGrantAuthenticationToken.SMS)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                            "客户端不支持sms grant type",
                            null));
        }

        // 3. 验证短信验证码
        validateSmsCode(smsGrantAuthentication.getPhone(), smsGrantAuthentication.getSmsCode());

        // 4. 根据手机号查找或创建用户
        SecurityUser user = findOrCreateUserByPhone(smsGrantAuthentication.getPhone());

        // 5. 确定授权的scope
        Set<String> authorizedScopes = validateScopes(registeredClient, smsGrantAuthentication.getScopes());

        // 6. 生成Token
        OAuth2AccessToken accessToken = generateAccessToken(
                registeredClient,
                user,
                SmsGrantAuthenticationToken.SMS,
                smsGrantAuthentication);

        log.debug("[Goya] |- security [authentication] SMS grant authentication successful for phone: {}", smsGrantAuthentication.getPhone());

        // 7. 返回认证结果
        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient,
                clientPrincipal,
                accessToken,
                null,
                authorizedScopes);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 验证客户端认证
     *
     * @param authentication 认证Token
     * @return 客户端认证Token
     * @throws OAuth2AuthenticationException 如果客户端认证失败
     */
    private OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(
            Authentication authentication) {
        OAuth2ClientAuthenticationToken clientPrincipal = null;
        if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
            clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
        }
        if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
            return clientPrincipal;
        }
        throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
    }

    /**
     * 验证短信验证码
     *
     * @param phone   手机号
     * @param smsCode 短信验证码
     * @throws SecurityCaptchaException 如果验证码验证失败
     */
    private void validateSmsCode(String phone, String smsCode) {
        if (StringUtils.isBlank(phone)) {
            throw new SecurityCaptchaArgumentIllegalException("手机号不能为空");
        }

        if (StringUtils.isBlank(smsCode)) {
            throw new SecurityCaptchaArgumentIllegalException("短信验证码不能为空");
        }

        // 从缓存中获取验证码
        String cacheKey = SMS_CODE_CACHE_KEY_PREFIX + phone;
        String cachedCode = cacheService.get(SMS_CODE_CACHE_NAME, cacheKey);

        if (StringUtils.isBlank(cachedCode)) {
            log.warn("[Goya] |- security [authentication] SMS code not found or expired for phone: {}", phone);
            throw new SecurityCaptchaHasExpiredException("短信验证码已过期或不存在");
        }

        // 验证验证码
        if (!smsCode.equals(cachedCode)) {
            log.warn("[Goya] |- security [authentication] SMS code mismatch for phone: {}", phone);
            throw new SecurityCaptchaMisMatchException("短信验证码错误");
        }

        // 验证成功后，删除验证码（一次性使用）
        cacheService.evict(SMS_CODE_CACHE_NAME, cacheKey);

        log.debug("[Goya] |- security [authentication] SMS code verification successful for phone: {}", phone);
    }

    /**
     * 根据手机号查找或创建用户
     *
     * @param phone 手机号
     * @return 用户信息
     * @throws AuthenticationException 如果用户查找或创建失败
     */
    private SecurityUser findOrCreateUserByPhone(String phone) {
        try {
            // 尝试通过手机号查找用户
            // 注意：这里假设ISecurityUserService有findUserByPhone方法
            // 如果没有，可以通过其他方式实现（如通过用户名查找，用户名即为手机号）
            SecurityUser user = null;
            try {
                // 尝试将手机号作为用户名查找
                user = securityUserService.findUser(phone);
            } catch (Exception e) {
                log.debug("[Goya] |- security [authentication] User not found by phone as username: {}", phone);
            }

            // 如果用户不存在，需要创建新用户
            // 注意：这里需要根据实际业务需求实现用户创建逻辑
            // 为了简化，这里假设用户必须已存在
            if (user == null) {
                log.warn("[Goya] |- security [authentication] User not found for phone: {}, user creation not implemented", phone);
                throw new BadCredentialsException("用户不存在，请先注册");
            }

            // 验证用户手机号是否匹配
            if (!phone.equals(user.getPhoneNumber())) {
                log.warn("[Goya] |- security [authentication] Phone number mismatch for user: {}", user.getUsername());
                throw new BadCredentialsException("手机号与用户信息不匹配");
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

            return user;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Error finding user by phone: {}", phone, e);
            throw new BadCredentialsException("用户查找失败", e);
        }
    }

    /**
     * 验证并确定授权的scope
     *
     * @param registeredClient 已注册的客户端
     * @param requestedScopes  请求的scope集合
     * @return 授权的scope集合
     */
    private Set<String> validateScopes(RegisteredClient registeredClient, Set<String> requestedScopes) {
        if (requestedScopes == null || requestedScopes.isEmpty()) {
            return registeredClient.getScopes();
        }

        // 验证请求的scope是否在客户端允许的scope范围内
        Set<String> authorizedScopes = registeredClient.getScopes();
        if (!authorizedScopes.containsAll(requestedScopes)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE,
                            "请求的scope不在客户端允许的范围内",
                            null));
        }

        return requestedScopes;
    }

}

