package com.ysmjjsy.goya.component.framework.servlet.web;

import com.ysmjjsy.goya.component.framework.core.web.RequestInfo;
import com.ysmjjsy.goya.component.framework.core.web.RequestInfoExtractor;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * <p>基于 Servlet 的请求信息提取器</p>
 *
 * @author goya
 * @since 2026/1/24 23:47
 */
public class ServletRequestInfoExtractor implements RequestInfoExtractor {

    @Override
    public RequestInfo extract() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (!(ra instanceof ServletRequestAttributes sra)) {
            return null;
        }
        HttpServletRequest req = sra.getRequest();
        return WebUtils.getRequestInfo(req);
    }
}
