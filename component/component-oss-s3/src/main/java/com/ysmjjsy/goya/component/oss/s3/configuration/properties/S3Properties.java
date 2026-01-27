package com.ysmjjsy.goya.component.oss.s3.configuration.properties;

import com.ysmjjsy.goya.component.oss.core.constants.OssConstants;
import com.ysmjjsy.goya.component.oss.core.properties.AbstractOssProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;

/**
 * <p>Amazon S3 配置参数</p>
 *
 * @author goya
 * @since 2025/11/1 17:36
 */
@ConfigurationProperties(prefix = OssConstants.PROPERTY_OSS_S3)
public class S3Properties extends AbstractOssProperties {

    @Serial
    private static final long serialVersionUID = -1620149117846471733L;


}