package com.ysmjjsy.goya.component.web.tenant;

import com.ysmjjsy.goya.component.core.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.tenant.TenantContext;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * <p>多租户拦截器</p>
 *
 * @author goya
 * @since 2025/10/15 09:37
 */
@Slf4j
public class MultiTenantInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        String tenantId = WebUtils.getTenantId(request);
        if (StringUtils.isBlank(tenantId)) {
            tenantId = DefaultConst.DEFAULT_TENANT_ID;
        }
        TenantContext.setTenantId(tenantId);
        log.debug("[Goya] |- TENANT ID is : [{}].", tenantId);

        String path = request.getRequestURI();
        String sessionId = WebUtils.getSessionId(request);
        String requestId = WebUtils.getRequestId(request);

        log.debug("[Goya] |- SESSION ID for [{}] is : [{}].", path, sessionId);
        log.debug("[Goya] |- SESSION ID of Goya for [{}] is : [{}].", path, requestId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        String path = request.getRequestURI();
        TenantContext.clear();
        log.debug("[Goya] |- Tenant Interceptor CLEAR tenantId for request [{}].", path);
    }
}
