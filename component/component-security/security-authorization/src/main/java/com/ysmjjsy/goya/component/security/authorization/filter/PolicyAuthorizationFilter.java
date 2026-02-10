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
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
        subjectContext.setAttributes(Map.of("principalName", authentication.getName()));

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

        Object bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (bestMatchingPattern instanceof String pattern && StringUtils.isNotBlank(pattern)) {
            return pattern;
        }
        return request.getRequestURI();
    }

    private String resolveTenant(HttpServletRequest request, Authentication authentication) {
        String requestTenant = request.getHeader(securityAuthorizationProperties.tenantHeader());
        if (StringUtils.isNotBlank(requestTenant)) {
            return requestTenant;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken().getClaimAsString(securityAuthorizationProperties.tenantClaim());
        }

        if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            Object tenant = bearerTokenAuthentication.getTokenAttributes().get(securityAuthorizationProperties.tenantClaim());
            return tenant == null ? null : String.valueOf(tenant);
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
}
