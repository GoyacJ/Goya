package com.ysmjjsy.goya.component.security.authorization.filter;

import com.ysmjjsy.goya.component.framework.security.api.AuthorizationService;
import com.ysmjjsy.goya.component.framework.security.api.AuthorizeRequest;
import com.ysmjjsy.goya.component.framework.security.context.ResourceContext;
import com.ysmjjsy.goya.component.framework.security.context.SubjectContext;
import com.ysmjjsy.goya.component.framework.security.decision.Decision;
import com.ysmjjsy.goya.component.framework.security.decision.DecisionType;
import com.ysmjjsy.goya.component.framework.security.domain.Action;
import com.ysmjjsy.goya.component.framework.security.domain.ResourceType;
import com.ysmjjsy.goya.component.framework.security.domain.SubjectType;
import com.ysmjjsy.goya.component.framework.servlet.scan.RestMappingCodeUtils;
import com.ysmjjsy.goya.component.security.authorization.configuration.properties.SecurityAuthorizationProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * <p>策略引擎授权过滤器</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class PolicyAuthorizationFilter extends OncePerRequestFilter {

    private final AuthorizationService authorizationService;
    private final SecurityAuthorizationProperties securityAuthorizationProperties;
    private final ObjectProvider<RequestMappingHandlerMapping> requestMappingHandlerMappingProvider;

    public PolicyAuthorizationFilter(AuthorizationService authorizationService,
                                     SecurityAuthorizationProperties securityAuthorizationProperties,
                                     ObjectProvider<RequestMappingHandlerMapping> requestMappingHandlerMappingProvider) {
        this.authorizationService = authorizationService;
        this.securityAuthorizationProperties = securityAuthorizationProperties;
        this.requestMappingHandlerMappingProvider = requestMappingHandlerMappingProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        AuthorizeRequest authorizeRequest = buildAuthorizeRequest(request, authentication);
        Decision decision = authorizationService.authorize(authorizeRequest);
        if (decision != null && DecisionType.DENY.equals(decision.getDecisionType())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    StringUtils.defaultIfBlank(decision.getReason(), "Forbidden by policy"));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private AuthorizeRequest buildAuthorizeRequest(HttpServletRequest request, Authentication authentication) {
        String pattern = resolveBestMatchingPattern(request);
        String mappingCode = RestMappingCodeUtils.createMappingCode(request.getMethod(), pattern);

        SubjectContext subjectContext = new SubjectContext();
        subjectContext.setSubjectType(SubjectType.USER);
        subjectContext.setSubjectId(resolveSubjectId(authentication));
        subjectContext.setAttributes(resolveSubjectAttributes(authentication));

        ResourceContext resourceContext = new ResourceContext();
        resourceContext.setResourceType(ResourceType.API);
        resourceContext.setResourceCode(mappingCode);
        resourceContext.setAttributes(Map.of(
                "method", request.getMethod(),
                "pattern", pattern,
                "requestUri", request.getRequestURI()
        ));

        Action action = new Action();
        action.setCode(securityAuthorizationProperties.apiAction());
        action.setName(securityAuthorizationProperties.apiAction());

        AuthorizeRequest authorizeRequest = new AuthorizeRequest();
        authorizeRequest.setTenantCode(resolveTenant(request, authentication));
        authorizeRequest.setSubjectContext(subjectContext);
        authorizeRequest.setResourceContext(resourceContext);
        authorizeRequest.setAction(action);
        authorizeRequest.setRequestTime(LocalDateTime.now());

        Map<String, Object> environment = new LinkedHashMap<>();
        environment.put("ip", request.getRemoteAddr());
        environment.put("uri", request.getRequestURI());
        environment.put("pattern", pattern);
        environment.put("mappingCode", mappingCode);
        authorizeRequest.setEnvironment(environment);
        return authorizeRequest;
    }

    private String resolveBestMatchingPattern(HttpServletRequest request) {
        Object bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (bestMatchingPattern instanceof String pattern && StringUtils.isNotBlank(pattern)) {
            return pattern;
        }

        RequestMappingHandlerMapping handlerMapping = requestMappingHandlerMappingProvider.getIfAvailable();
        if (handlerMapping != null) {
            List<RequestMappingInfo> matchedInfos = new ArrayList<>();
            for (RequestMappingInfo requestMappingInfo : handlerMapping.getHandlerMethods().keySet()) {
                RequestMappingInfo matchingInfo = requestMappingInfo.getMatchingCondition(request);
                if (matchingInfo != null) {
                    matchedInfos.add(matchingInfo);
                }
            }
            if (!matchedInfos.isEmpty()) {
                RequestMappingInfo best = matchedInfos.get(0);
                for (int index = 1; index < matchedInfos.size(); index++) {
                    RequestMappingInfo candidate = matchedInfos.get(index);
                    if (candidate.compareTo(best, request) < 0) {
                        best = candidate;
                    }
                }
                if (best.getPathPatternsCondition() != null && best.getPathPatternsCondition().getFirstPattern() != null) {
                    return best.getPathPatternsCondition().getFirstPattern().getPatternString();
                }
            }
        }
        return request.getRequestURI();
    }

    private String resolveTenant(HttpServletRequest request, Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String tenant = jwtAuthenticationToken.getToken().getClaimAsString(securityAuthorizationProperties.tenantClaim());
            if (StringUtils.isNotBlank(tenant)) {
                return tenant;
            }
        }

        if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            Object tenant = bearerTokenAuthentication.getTokenAttributes().get(securityAuthorizationProperties.tenantClaim());
            if (tenant != null && StringUtils.isNotBlank(String.valueOf(tenant))) {
                return String.valueOf(tenant);
            }
        }

        String requestTenant = request.getHeader(securityAuthorizationProperties.tenantHeader());
        if (StringUtils.isNotBlank(requestTenant)) {
            return requestTenant;
        }

        return null;
    }

    private String resolveSubjectId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String subject = jwtAuthenticationToken.getToken().getSubject();
            if (StringUtils.isNotBlank(subject)) {
                return subject;
            }
        }

        if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            Object sub = bearerTokenAuthentication.getTokenAttributes().get("sub");
            if (sub != null) {
                return String.valueOf(sub);
            }
        }

        return authentication.getName();
    }

    private Map<String, Object> resolveSubjectAttributes(Authentication authentication) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("principalName", authentication.getName());

        Map<String, Object> tokenAttributes = resolveTokenAttributes(authentication);
        List<String> roleIds = readStringList(tokenAttributes.get(securityAuthorizationProperties.roleIdsClaim()));
        if (roleIds.isEmpty()) {
            roleIds = readStringList(tokenAttributes.get("roles"));
        }
        List<String> teamIds = readStringList(tokenAttributes.get(securityAuthorizationProperties.teamIdsClaim()));
        List<String> orgIds = readStringList(tokenAttributes.get(securityAuthorizationProperties.orgIdsClaim()));

        if (!roleIds.isEmpty()) {
            attributes.put("roleIds", roleIds);
        }
        if (!teamIds.isEmpty()) {
            attributes.put("teamIds", teamIds);
        }
        if (!orgIds.isEmpty()) {
            attributes.put("orgIds", orgIds);
        }

        return attributes;
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
            Map<String, Object> attributes = new LinkedHashMap<>();
            principalMap.forEach((key, value) -> attributes.put(String.valueOf(key), value));
            return attributes;
        }
        return Collections.emptyMap();
    }

    private List<String> readStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof String text) {
            if (StringUtils.isBlank(text)) {
                return List.of();
            }
            String[] parts = text.split(",");
            List<String> values = new ArrayList<>(parts.length);
            for (String part : parts) {
                if (StringUtils.isNotBlank(part)) {
                    values.add(part.trim());
                }
            }
            return values;
        }
        if (value instanceof Collection<?> collection) {
            LinkedHashSet<String> values = new LinkedHashSet<>();
            for (Object item : collection) {
                if (item == null) {
                    continue;
                }
                String text = String.valueOf(item).trim();
                if (StringUtils.isNotBlank(text)) {
                    values.add(text);
                }
            }
            return new ArrayList<>(values);
        }
        return List.of(String.valueOf(value));
    }
}
