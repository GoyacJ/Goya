package com.ysmjjsy.goya.component.security.oauth2.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * <p>租户路径包装器：去除 /t/{tenant} 前缀</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public class TenantPathRequestWrapper extends HttpServletRequestWrapper {

    private final String originalRequestUri;
    private final String originalRequestUrl;
    private final String strippedRequestUri;

    public TenantPathRequestWrapper(HttpServletRequest request,
                                    String originalRequestUri,
                                    String originalRequestUrl,
                                    String strippedRequestUri) {
        super(request);
        this.originalRequestUri = originalRequestUri;
        this.originalRequestUrl = originalRequestUrl;
        this.strippedRequestUri = strippedRequestUri;
    }

    @Override
    public String getRequestURI() {
        return strippedRequestUri;
    }

    @Override
    public String getServletPath() {
        return strippedRequestUri;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(originalRequestUrl);
    }

    public String getOriginalRequestUri() {
        return originalRequestUri;
    }
}
