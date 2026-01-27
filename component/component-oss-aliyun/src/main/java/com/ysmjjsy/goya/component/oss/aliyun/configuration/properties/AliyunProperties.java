package com.ysmjjsy.goya.component.oss.aliyun.configuration.properties;

import com.ysmjjsy.goya.component.framework.oss.constants.OssConstants;
import com.ysmjjsy.goya.component.framework.oss.properties.AbstractOssProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;

/**
 * <p>Aliyun OSS 配置参数</p>
 *
 * @author goya
 * @since 2025/11/1 17:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = OssConstants.PROPERTY_OSS_ALIYUN)
public class AliyunProperties extends AbstractOssProperties {

    @Serial
    private static final long serialVersionUID = -1640785211985399028L;
    /**
     * 授权STSAssumeRole访问的Region。以华东1（杭州）为例，其它Region请根据实际情况填写。
     */
    private String region;
    /**
     * AM角色的RamRoleArn
     */
    private String role;
}
