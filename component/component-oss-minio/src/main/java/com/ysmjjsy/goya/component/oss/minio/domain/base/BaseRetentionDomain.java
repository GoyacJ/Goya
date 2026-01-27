package com.ysmjjsy.goya.component.oss.minio.domain.base;

import com.ysmjjsy.goya.component.framework.common.pojo.IEntity;
import com.ysmjjsy.goya.component.oss.minio.enums.RetentionModeEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

/**
 * <p>Retention 相关共用属性抽象类</p>
 *
 * @author goya
 * @since 2025/11/1 15:47
 */
@Data
public abstract class BaseRetentionDomain implements IEntity {

    @Serial
    private static final long serialVersionUID = -5112595104526794497L;

    @Schema(name = "保留模式")
    private RetentionModeEnums mode;
}
