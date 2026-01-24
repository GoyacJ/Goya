package com.ysmjjsy.goya.component.security.core.tenant;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * <p>基于路径的租户解析器</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public class PathTenantIdResolver implements TenantIdResolver {

    private final String pathPrefix;

    public PathTenantIdResolver(String pathPrefix) {
        String normalized = StringUtils.defaultIfBlank(pathPrefix, "/t");
        this.pathPrefix = normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    @Override
    public @Nullable String resolveTenantId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String prefix = pathPrefix.endsWith("/") ? pathPrefix : pathPrefix + "/";
        if (StringUtils.isBlank(uri) || !uri.startsWith(prefix)) {
            return null;
        }
        String remaining = uri.substring(prefix.length());
        int slashIndex = remaining.indexOf('/');
        if (slashIndex < 0) {
            return remaining;
        }
        String tenantId = remaining.substring(0, slashIndex);
        return StringUtils.isNotBlank(tenantId) ? tenantId : null;
    }
}
