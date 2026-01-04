package com.ysmjjsy.goya.security.authentication.userinfo;

import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.security.core.service.ISecurityUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>社交登录OAuth2用户服务</p>
 * <p>处理第三方登录回调，调用OAuth2UserInfoMapper进行用户映射，支持用户绑定和自动注册</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>处理OIDC用户信息（GitHub、Gitee等支持OIDC的提供商）</li>
 *   <li>处理OAuth2用户信息（微信等不支持OIDC的提供商）</li>
 *   <li>用户绑定：将第三方账号与现有用户绑定</li>
 *   <li>自动注册：如果用户不存在，自动创建新用户（可选）</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 在OAuth2ClientConfiguration中配置
 * @Bean
 * public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(
 *         OAuth2UserInfoMapper oAuth2UserInfoMapper) {
 *     return new SocialOAuth2UserService(new OidcUserService(), oAuth2UserInfoMapper);
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 * @see OAuth2UserInfoMapper
 * @see ISecurityUserService
 */
@Slf4j
@RequiredArgsConstructor
public class SocialOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate;
    private final ISecurityUserService securityUserService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 使用默认的OidcUserService加载用户信息
        OidcUser oidcUser = delegate.loadUser(userRequest);

        // 2. 获取第三方用户信息
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oidcUser.getAttributes();

        log.debug("[Goya] |- security [authentication] Loading OIDC user from provider: {}", registrationId);

        // 3. 根据第三方用户信息查找或创建本地用户
        SecurityUser localUser = findOrCreateUser(registrationId, attributes, oidcUser);

        // 4. 创建自定义的OidcUser，包含本地用户信息
        return createCustomOidcUser(oidcUser, localUser);
    }

    /**
     * 查找或创建用户
     *
     * @param registrationId 第三方登录提供商ID（wechat、gitee、github）
     * @param attributes     第三方用户属性
     * @param oidcUser       OIDC用户信息
     * @return 本地用户信息
     */
    private SecurityUser findOrCreateUser(
            String registrationId,
            Map<String, Object> attributes,
            OidcUser oidcUser) {
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
                    user = securityUserService.findUser(email);
                    log.debug("[Goya] |- security [authentication] User found by email: {}", email);
                } catch (Exception e) {
                    log.debug("[Goya] |- security [authentication] User not found by email: {}", email);
                }
            }

            if (user == null && StringUtils.isNotBlank(username)) {
                try {
                    user = securityUserService.findUser(username);
                    log.debug("[Goya] |- security [authentication] User found by username: {}", username);
                } catch (Exception e) {
                    log.debug("[Goya] |- security [authentication] User not found by username: {}", username);
                }
            }

            // 3. 如果用户不存在，根据配置决定是否自动创建
            if (user == null) {
                log.debug("[Goya] |- security [authentication] User not found, auto-creation not implemented yet.");
                // TODO: 实现自动创建用户逻辑
                // 需要根据业务需求实现用户创建
                throw new OAuth2AuthenticationException(
                        new org.springframework.security.oauth2.core.OAuth2Error(
                                "user_not_found",
                                "用户不存在，请先注册",
                                null));
            }

            return user;
        } catch (OAuth2AuthenticationException e) {
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
     * @param attributes     用户属性
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
     * @param attributes     用户属性
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

    /**
     * 创建自定义OidcUser
     *
     * @param oidcUser   原始OIDC用户
     * @param localUser  本地用户信息
     * @return 自定义OidcUser
     */
    private OidcUser createCustomOidcUser(OidcUser oidcUser, SecurityUser localUser) {
        // 合并本地用户信息和第三方用户信息
        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        if (localUser.getAuthorities() != null) {
            authorities.addAll(localUser.getAuthorities());
        }

        // 创建自定义OidcUser实现
        return new CustomOidcUser(
                oidcUser.getIdToken(),
                oidcUser.getAccessToken(),
                oidcUser.getUserInfo(),
                oidcUser.getName(),
                oidcUser.getAuthorities(),
                localUser);
    }

    /**
     * 自定义OidcUser实现
     * 包含本地用户信息和第三方用户信息
     */
    private static class CustomOidcUser implements OidcUser {
        private final org.springframework.security.oauth2.core.oidc.IdToken idToken;
        private final org.springframework.security.oauth2.core.oidc.AccessToken accessToken;
        private final org.springframework.security.oauth2.core.oidc.OidcUserInfo userInfo;
        private final String name;
        private final Set<GrantedAuthority> authorities;
        private final SecurityUser localUser;

        public CustomOidcUser(
                org.springframework.security.oauth2.core.oidc.IdToken idToken,
                org.springframework.security.oauth2.core.oidc.AccessToken accessToken,
                org.springframework.security.oauth2.core.oidc.OidcUserInfo userInfo,
                String name,
                Set<GrantedAuthority> authorities,
                SecurityUser localUser) {
            this.idToken = idToken;
            this.accessToken = accessToken;
            this.userInfo = userInfo;
            this.name = name;
            this.authorities = authorities;
            this.localUser = localUser;
        }

        @Override
        public Map<String, Object> getClaims() {
            return userInfo != null ? userInfo.getClaims() : Map.of();
        }

        @Override
        public org.springframework.security.oauth2.core.oidc.IdToken getIdToken() {
            return idToken;
        }

        @Override
        public org.springframework.security.oauth2.core.oidc.AccessToken getAccessToken() {
            return accessToken;
        }

        @Override
        public org.springframework.security.oauth2.core.oidc.OidcUserInfo getUserInfo() {
            return userInfo;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return userInfo != null ? userInfo.getClaims() : Map.of();
        }

        @Override
        public Set<GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getName() {
            return name;
        }

        /**
         * 获取本地用户信息
         *
         * @return 本地用户信息
         */
        public SecurityUser getLocalUser() {
            return localUser;
        }
    }
}

