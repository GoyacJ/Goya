package com.ysmjjsy.goya.component.oss.proxy;

import com.ysmjjsy.goya.component.core.constants.SymbolConst;
import com.ysmjjsy.goya.component.oss.configuration.properties.OssProperties;
import com.ysmjjsy.goya.component.oss.core.constants.OssConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * <p>默认代理地址转换器</p>
 *
 * @author goya
 * @since 2025/11/3 09:39
 */
public class OssProxyAddressFunction implements Function<HttpServletRequest, String> {

    private static final Logger log = LoggerFactory.getLogger(OssProxyAddressFunction.class);

    private final OssProperties ossProperties;

    public OssProxyAddressFunction(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public String apply(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String path = uri.replace(OssConstants.PRESIGNED_OBJECT_URL_PROXY, SymbolConst.BLANK);

        String queryString = request.getQueryString();
        String params = queryString != null ? SymbolConst.QUESTION + queryString : SymbolConst.BLANK;

        String target = ossProperties.destination() + path + params;
        log.debug("[HZ-ZHG] |- Convert request [{}] to [{}].", uri, target);
        return target;
    }
}