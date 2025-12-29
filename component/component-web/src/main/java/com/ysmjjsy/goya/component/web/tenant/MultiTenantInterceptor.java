package com.ysmjjsy.goya.component.web.tenant;

import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import com.ysmjjsy.goya.component.common.tenant.TenantContext;
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
            tenantId = IBaseConstants.DEFAULT_TENANT_ID;
        }
        TenantContext.setTenantId(tenantId);
        log.debug("[GOYA] |- TENANT ID is : [{}].", tenantId);

        String path = request.getRequestURI();
        String sessionId = WebUtils.getSessionId(request);
        String requestId = WebUtils.getRequestId(request);

        log.debug("[GOYA] |- SESSION ID for [{}] is : [{}].", path, sessionId);
        log.debug("[GOYA] |- SESSION ID of Goya for [{}] is : [{}].", path, requestId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        String path = request.getRequestURI();
        TenantContext.clear();
        log.debug("[GOYA] |- Tenant Interceptor CLEAR tenantId for request [{}].", path);
    }
}
