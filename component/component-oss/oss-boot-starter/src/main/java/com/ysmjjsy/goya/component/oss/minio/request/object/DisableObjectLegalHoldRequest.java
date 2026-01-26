package com.ysmjjsy.goya.component.oss.minio.request.object;

import com.ysmjjsy.goya.component.oss.minio.definition.ObjectVersionRequest;
import io.minio.DisableObjectLegalHoldArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> 设置关闭对象持有配置 </p>
 *
 * @author goya
 * @since 2023/6/11 10:20
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "设置关闭对象持有配置")
public class DisableObjectLegalHoldRequest extends ObjectVersionRequest<DisableObjectLegalHoldArgs.Builder, DisableObjectLegalHoldArgs> {
    @Serial
    private static final long serialVersionUID = -4729610172625384775L;

    @Override
    public DisableObjectLegalHoldArgs.Builder getBuilder() {
        return DisableObjectLegalHoldArgs.builder();
    }
}
