package com.ysmjjsy.goya.component.web.secure;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/9 16:23
 */
@Slf4j
public class XssHttpServletFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(request);
        log.trace("[GOYA] |- XssHttpServletFilter wrapper request for [{}].", request.getRequestURI());
        filterChain.doFilter(xssRequest, servletResponse);
    }
}
