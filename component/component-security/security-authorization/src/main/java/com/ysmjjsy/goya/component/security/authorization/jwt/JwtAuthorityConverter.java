package com.ysmjjsy.goya.component.security.authorization.jwt;

import com.ysmjjsy.goya.security.core.constants.IStandardClaimNamesConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>JWT权限转换器</p>
 * <p>从JWT中提取roles和authorities，转换为Spring Security的GrantedAuthority</p>
 * <p>支持多租户场景下的权限管理</p>
 *
 * <p>提取逻辑：</p>
 * <ul>
 *   <li>从JWT的roles claim提取角色，转换为ROLE_xxx格式的权限</li>
 *   <li>从JWT的authorities claim提取权限，直接使用</li>
 *   <li>从JWT的scope claim提取范围，转换为SCOPE_xxx格式的权限</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
public class JwtAuthorityConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. 从roles claim提取角色（转换为ROLE_xxx格式）
        Object rolesObj = jwt.getClaim(IStandardClaimNamesConstants.ROLES);
        if (rolesObj instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) rolesObj;
            for (String role : roles) {
                if (role != null && !role.isEmpty()) {
                    // 确保角色以ROLE_前缀开头
                    String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    authorities.add(new SimpleGrantedAuthority(authority));
                    log.trace("[Goya] |- security [resource] Added role authority: {}", authority);
                }
            }
        }

        // 2. 从authorities claim提取权限（直接使用）
        Object authoritiesObj = jwt.getClaim(IStandardClaimNamesConstants.AUTHORITIES);
        if (authoritiesObj instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> auths = (Collection<String>) authoritiesObj;
            for (String authority : auths) {
                if (authority != null && !authority.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(authority));
                    log.trace("[Goya] |- security [resource] Added authority: {}", authority);
                }
            }
        }

        // 3. 从scope claim提取范围（转换为SCOPE_xxx格式）
        List<String> scopes = jwt.getClaimAsStringList("scope");
        if (scopes != null) {
            for (String scope : scopes) {
                if (scope != null && !scope.isEmpty()) {
                    String authority = "SCOPE_" + scope;
                    authorities.add(new SimpleGrantedAuthority(authority));
                    log.trace("[Goya] |- security [resource] Added scope authority: {}", authority);
                }
            }
        }

        log.debug("[Goya] |- security [resource] Extracted {} authorities from JWT for subject: {}", 
                authorities.size(), jwt.getSubject());

        return authorities;
    }
}

