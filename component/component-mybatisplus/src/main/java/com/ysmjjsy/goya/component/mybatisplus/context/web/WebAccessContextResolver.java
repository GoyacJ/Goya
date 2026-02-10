package com.ysmjjsy.goya.component.mybatisplus.context.web;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.core.context.GoyaContext;
import com.ysmjjsy.goya.component.framework.core.context.GoyaUser;
import com.ysmjjsy.goya.component.framework.core.context.SpringContext;
import com.ysmjjsy.goya.component.framework.security.domain.SubjectType;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextResolver;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextValue;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * <p>基于请求头的访问上下文解析器。</p>
 *
 * <p>默认读取 X-User-Id，并映射为 Subject(USER)。</p>
 *
 * @author goya
 * @since 2026/1/31 13:30
 */
public class WebAccessContextResolver implements AccessContextResolver {

    @Override
    public AccessContextValue resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        AccessContextValue fromUnifiedContext = resolveFromUnifiedContext(request);
        if (fromUnifiedContext != null) {
            return fromUnifiedContext;
        }

        if (StringUtils.hasText(WebUtils.getBearerToken(request))) {
            return null;
        }

        String userId = headerOrNull(request, DefaultConst.X_USER_ID);
        if (!StringUtils.hasText(userId)) {
            return null;
        }

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("userId", userId);
        String tenantId = headerOrNull(request, DefaultConst.X_TENANT_ID);
        if (StringUtils.hasText(tenantId)) {
            attrs.put("tenantId", tenantId);
        }
        return new AccessContextValue(userId, SubjectType.USER, userId, attrs);
    }

    private String headerOrNull(HttpServletRequest request, String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        String value = request.getHeader(name);
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private AccessContextValue resolveFromUnifiedContext(HttpServletRequest request) {
        GoyaContext goyaContext = SpringContext.getBeanOrNull(GoyaContext.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            return null;
        }

        GoyaUser goyaUser = goyaContext == null ? null : goyaContext.currentUser(request);
        String subjectId = resolveSubjectId(goyaUser, authentication);
        if (!StringUtils.hasText(subjectId)) {
            return null;
        }

        Map<String, Object> tokenAttributes = resolveTokenAttributes(authentication);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("userId", subjectId);
        attrs.put("subjectSource", "goya-context");
        String tenantId = goyaContext == null ? null : goyaContext.currentTenant();
        if (StringUtils.hasText(tenantId)) {
            attrs.put("tenantId", tenantId);
        }

        List<String> roleIds = readStringList(tokenAttributes.get("role_ids"));
        if (CollectionUtils.isEmpty(roleIds)) {
            roleIds = readStringList(tokenAttributes.get("roles"));
        }
        List<String> teamIds = readStringList(tokenAttributes.get("team_ids"));
        List<String> orgIds = readStringList(tokenAttributes.get("org_ids"));

        if (!CollectionUtils.isEmpty(roleIds)) {
            attrs.put("roleIds", roleIds);
        }
        if (!CollectionUtils.isEmpty(teamIds)) {
            attrs.put("teamIds", teamIds);
        }
        if (!CollectionUtils.isEmpty(orgIds)) {
            attrs.put("orgIds", orgIds);
        }

        return new AccessContextValue(subjectId, SubjectType.USER, subjectId, attrs);
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String resolveSubjectId(GoyaUser goyaUser, Authentication authentication) {
        if (goyaUser != null && StringUtils.hasText(goyaUser.getUserId())) {
            return goyaUser.getUserId();
        }

        Map<String, Object> tokenAttributes = resolveTokenAttributes(authentication);
        Object sub = tokenAttributes.get("sub");
        if (sub != null && StringUtils.hasText(String.valueOf(sub))) {
            return String.valueOf(sub);
        }

        return StringUtils.hasText(authentication.getName()) ? authentication.getName() : null;
    }

    private Map<String, Object> resolveTokenAttributes(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken().getClaims();
        }

        if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            return bearerTokenAuthentication.getTokenAttributes();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2AuthenticatedPrincipal oauth2AuthenticatedPrincipal) {
            return oauth2AuthenticatedPrincipal.getAttributes();
        }
        if (principal instanceof Map<?, ?> principalMap) {
            Map<String, Object> map = new LinkedHashMap<>();
            principalMap.forEach((key, value) -> map.put(String.valueOf(key), value));
            return map;
        }

        return Map.of();
    }

    private List<String> readStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof String text) {
            if (!StringUtils.hasText(text)) {
                return List.of();
            }
            String[] parts = text.split(",");
            List<String> result = new ArrayList<>(parts.length);
            for (String part : parts) {
                if (StringUtils.hasText(part)) {
                    result.add(part.trim());
                }
            }
            return result;
        }
        if (value instanceof Collection<?> collection) {
            LinkedHashSet<String> result = new LinkedHashSet<>();
            for (Object item : collection) {
                if (item == null) {
                    continue;
                }
                String text = String.valueOf(item).trim();
                if (StringUtils.hasText(text)) {
                    result.add(text);
                }
            }
            return new ArrayList<>(result);
        }
        String text = String.valueOf(value).trim();
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        return List.of(text);
    }
}
