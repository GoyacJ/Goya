package com.ysmjjsy.goya.component.oss.minio.configuration.properties;

import com.ysmjjsy.goya.component.oss.core.constants.OssConstants;
import com.ysmjjsy.goya.component.oss.core.properties.AbstractOssProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/11/1 16:07
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = OssConstants.PROPERTY_OSS_MINIO)
public class MinioProperties extends AbstractOssProperties {

    @Serial
    private static final long serialVersionUID = 1431213979270710695L;

}