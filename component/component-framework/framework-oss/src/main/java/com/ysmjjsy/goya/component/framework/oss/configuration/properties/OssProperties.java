package com.ysmjjsy.goya.component.framework.oss.configuration.properties;

import com.ysmjjsy.goya.component.framework.oss.constants.OssConstants;
import com.ysmjjsy.goya.component.framework.oss.enums.OssEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * <p>OSS 配置</p>
 *
 * @author goya
 * @since 2025/11/3 09:28
 */
@ConfigurationProperties(prefix = OssConstants.PROPERTY_PREFIX_OSS)
public record OssProperties(

        /*
         * 是否启用代理，防止前端直接访问
         */
        @DefaultValue("true")
        Boolean useProxy,

        /*
         * 代理请求发送源地址。例如：前端 http://localhost:3000。注意如果有前端有配置代理需要加上
         */
        String proxySourceEndpoint,


        /*
         * 采用 Minio SDK 作为默认实现
         */
        @DefaultValue("minio")
        OssEnum type,

        /*
         * 代理请求转发目的地址
         */
        String destination
) {
}
