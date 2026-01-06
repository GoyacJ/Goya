package com.ysmjjsy.goya.security.authentication.provider.login;

import com.ysmjjsy.goya.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.security.authentication.provider.AbstractAuthenticationProvider;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.security.core.manager.SecurityUserManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

/**
 * <p>社交登录认证提供者</p>
 * <p>处理第三方社交登录方式的认证（微信、Gitee、GitHub等）</p>
 * <p>符合Spring Security标准模式，实现AuthenticationProvider接口</p>
 * <p>用于Authorization Code流程，返回UsernamePasswordAuthenticationToken让SAS框架处理授权码生成</p>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>接收SocialAuthenticationToken（包含社交提供商ID和用户属性）</li>
 *   <li>查找或创建用户</li>
 *   <li>返回UsernamePasswordAuthenticationToken（让SAS框架处理授权码生成）</li>
 * </ol>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class SocialAuthenticationProvider extends AbstractAuthenticationProvider {

    private final SecurityUserManager securityUserService;
    private final SecurityAuthenticationProperties authenticationProperties;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SocialAuthenticationToken socialToken = (SocialAuthenticationToken) authentication;

        String registrationId = socialToken.getSocialProviderId();
        Map<String, Object> attributes = socialToken.getSocialUserAttributes();

        log.debug("[Goya] |- security [authentication] Authenticating user with social login: {}", registrationId);

        // 查找或创建用户
        SecurityUser user = findOrCreateUser(registrationId, attributes);

        // 返回 Spring Security 标准认证对象
        // 关键：这里返回 UsernamePasswordAuthenticationToken，让 SAS 框架处理授权码生成
        UsernamePasswordAuthenticationToken authenticatedToken =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,  // credentials 已擦除
                        user.getAuthorities()
                );

        log.debug("[Goya] |- security [authentication] Social login authentication successful for provider: {}, returning UsernamePasswordAuthenticationToken for Authorization Code flow",
                registrationId);

        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SocialAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 查找或创建用户
     *
     * @param registrationId 第三方登录提供商ID（wechat、gitee、github）
     * @param attributes      第三方用户属性
     * @return 本地用户信息
     * @throws AuthenticationException 如果用户查找或创建失败
     */
    private SecurityUser findOrCreateUser(String registrationId, Map<String, Object> attributes) {
        try {
            // 1. 尝试通过第三方唯一标识查找用户（如openid、id等）
            String thirdPartyId = extractThirdPartyId(registrationId, attributes);
            if (StringUtils.isNotBlank(thirdPartyId)) {
                // TODO: 实现通过第三方ID查找用户的逻辑
                // 这里需要扩展ISecurityUserService支持按第三方ID查找
                log.debug("[Goya] |- security [authentication] Looking up user by third-party ID: {}", thirdPartyId);
            }

            // 2. 尝试通过邮箱或用户名查找用户
            String email = extractEmail(attributes);
            String username = extractUsername(registrationId, attributes);

            SecurityUser user = null;
            if (StringUtils.isNotBlank(email)) {
                try {
                    // 尝试通过邮箱查找（假设邮箱可以作为用户名）
                    user = securityUserService.findUserByUsername(email);
                    log.debug("[Goya] |- security [authentication] User found by email: {}", email);
                } catch (Exception e) {
                    log.debug("[Goya] |- security [authentication] User not found by email: {}", email);
                }
            }

            if (user == null && StringUtils.isNotBlank(username)) {
                try {
                    user = securityUserService.findUserByUsername(username);
                    log.debug("[Goya] |- security [authentication] User found by username: {}", username);
                } catch (Exception e) {
                    log.debug("[Goya] |- security [authentication] User not found by username: {}", username);
                }
            }

            // 3. 如果用户不存在，根据配置决定是否自动创建
            if (user == null) {
                SecurityAuthenticationProperties.SocialLoginConfig socialLoginConfig = authenticationProperties.socialLogin();
                if (socialLoginConfig != null && Boolean.TRUE.equals(socialLoginConfig.autoCreateUser())) {
                    try {
                        // 自动创建用户
                        String defaultRole = socialLoginConfig.defaultRole();
                        user = securityUserService.createUserFromSocialLogin(registrationId, attributes, defaultRole);
                        log.info("[Goya] |- security [authentication] User auto-created from social login: {} | provider: {}", 
                                user.getUsername(), registrationId);
                    } catch (UnsupportedOperationException e) {
                        log.warn("[Goya] |- security [authentication] createUserFromSocialLogin not implemented, cannot auto-create user");
                        throw new OAuth2AuthenticationException(
                                new org.springframework.security.oauth2.core.OAuth2Error(
                                        "user_not_found",
                                        "用户不存在，请先注册",
                                        null));
                    } catch (Exception e) {
                        log.error("[Goya] |- security [authentication] Failed to auto-create user from social login", e);
                        throw new OAuth2AuthenticationException(
                                new org.springframework.security.oauth2.core.OAuth2Error(
                                        "user_creation_failed",
                                        "用户自动创建失败: " + e.getMessage(),
                                        null),
                                e);
                    }
                } else {
                    log.debug("[Goya] |- security [authentication] User not found and auto-creation is disabled");
                    throw new OAuth2AuthenticationException(
                            new org.springframework.security.oauth2.core.OAuth2Error(
                                    "user_not_found",
                                    "用户不存在，请先注册",
                                    null));
                }
            }

            // 4. 检查账户状态
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
        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Error finding or creating user from social login", e);
            throw new OAuth2AuthenticationException(
                    new org.springframework.security.oauth2.core.OAuth2Error(
                            "user_lookup_failed",
                            "用户查找失败: " + e.getMessage(),
                            null),
                    e);
        }
    }

    /**
     * 提取第三方唯一标识
     *
     * @param registrationId 第三方登录提供商ID
     * @param attributes      用户属性
     * @return 第三方唯一标识
     */
    private String extractThirdPartyId(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "wechat" -> (String) attributes.get("openid");
            case "gitee", "github" -> String.valueOf(attributes.get("id"));
            default -> null;
        };
    }

    /**
     * 提取邮箱
     *
     * @param attributes 用户属性
     * @return 邮箱地址
     */
    private String extractEmail(Map<String, Object> attributes) {
        Object email = attributes.get("email");
        return email != null ? email.toString() : null;
    }

    /**
     * 提取用户名
     *
     * @param registrationId 第三方登录提供商ID
     * @param attributes      用户属性
     * @return 用户名
     */
    private String extractUsername(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "wechat" -> (String) attributes.get("nickname");
            case "gitee" -> (String) attributes.get("login");
            case "github" -> (String) attributes.get("login");
            default -> (String) attributes.get("name");
        };
    }
}

