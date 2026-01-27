package com.ysmjjsy.goya.component.oss.minio.domain;

import com.ysmjjsy.goya.component.oss.minio.domain.base.BaseRetentionDomain;
import com.ysmjjsy.goya.component.oss.minio.enums.RetentionUnitEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>Minio ObjectLockConfiguration 对应 Domain Object</p>
 *
 * @author goya
 * @since 2025/11/1 15:50
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "存储桶保留设置域对象")
public class ObjectLockConfigurationDomain extends BaseRetentionDomain {

    @Serial
    private static final long serialVersionUID = 352929002836501597L;

    @Schema(name = "保留周期")
    private RetentionUnitEnums unit;

    @Schema(name = "保留有效期")
    private Integer validity;
}
