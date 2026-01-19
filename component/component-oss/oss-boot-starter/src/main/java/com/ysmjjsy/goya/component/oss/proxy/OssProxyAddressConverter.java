package com.ysmjjsy.goya.component.oss.proxy;

import com.ysmjjsy.goya.component.oss.configuration.properties.OssProperties;
import com.ysmjjsy.goya.component.oss.core.constants.OssConstants;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>默认代理地址转换器</p>
 *
 * @author goya
 * @since 2025/11/3 09:38
 */
public class OssProxyAddressConverter implements Converter<String, String> {

    private static final Logger log = LoggerFactory.getLogger(OssProxyAddressConverter.class);

    private final OssProperties ossProperties;

    public OssProxyAddressConverter(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public String convert(String source) {
        if (ossProperties.useProxy()) {
            String endpoint = ossProperties.proxySourceEndpoint() + OssConstants.PRESIGNED_OBJECT_URL_PROXY;
            String target = Strings.CS.replace(source, ossProperties.destination(), endpoint);
            log.debug("[Goya] |- Convert presignedObjectUrl [{}] to [{}].", endpoint, target);
            return target;
        }

        return source;
    }
}
