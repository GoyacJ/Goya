package com.ysmjjsy.goya.component.oss.minio.bo;

import com.ysmjjsy.goya.component.framework.common.pojo.IEntity;
import com.ysmjjsy.goya.component.oss.minio.domain.ObjectLockConfigurationDomain;
import com.ysmjjsy.goya.component.oss.minio.domain.VersioningConfigurationDomain;
import com.ysmjjsy.goya.component.oss.minio.enums.PolicyEnums;
import com.ysmjjsy.goya.component.oss.minio.enums.SseConfigurationEnums;
import lombok.Data;

import java.io.Serial;
import java.util.Map;

/**
 * <p> 存储桶基础信息返回实体 </p>
 *
 * @author goya
 * @since 2023/6/5 20:41
 */
@Data
public class BucketSettingBusiness implements IEntity {

    @Serial
    private static final long serialVersionUID = -140601667288424174L;
    
    /**
     * 服务端加密方式
     */
    private SseConfigurationEnums sseConfiguration;

    private PolicyEnums policy;
    /**
     * 标签
     */
    private Map<String, String> tags;

    /**
     * 对象锁定是否开启
     */
    private ObjectLockConfigurationDomain objectLock;

    /**
     * 配额大小
     */
    private Long quota;
    /**
     * 版本设置配置
     */
    private VersioningConfigurationDomain versioning;
}
