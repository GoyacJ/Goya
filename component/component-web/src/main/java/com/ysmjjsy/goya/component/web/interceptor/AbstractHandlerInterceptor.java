package com.ysmjjsy.goya.component.web.interceptor;

import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import com.ysmjjsy.goya.component.common.utils.MD5Utils;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/9 16:17
 */
public abstract class AbstractHandlerInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AbstractHandlerInterceptor.class);

    protected String generateRequestKey(HttpServletRequest request) {

        String requestId = WebUtils.getRequestId(request);

        String url = request.getRequestURI();
        String method = request.getMethod();

        if (StringUtils.isNotBlank(requestId)) {
            String key = MD5Utils.md5(requestId + ISymbolConstants.COLON + url + ISymbolConstants.COLON + method);
            log.debug("[GOYA] |- IdempotentInterceptor key is [{}].", key);
            return key;
        } else {
            log.warn("[GOYA] |- IdempotentInterceptor cannot create key, because requestId is null.");
            return null;
        }
    }
}