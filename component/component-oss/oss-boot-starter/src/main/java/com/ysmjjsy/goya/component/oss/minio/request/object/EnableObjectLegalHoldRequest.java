package com.ysmjjsy.goya.component.oss.minio.request.object;

import com.ysmjjsy.goya.component.oss.minio.definition.ObjectVersionRequest;
import io.minio.EnableObjectLegalHoldArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> 设置开启对象持有配置 </p>
 *
 * @author goya
 * @since 2023/6/11 10:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "设置开启对象持有配置")
public class EnableObjectLegalHoldRequest extends ObjectVersionRequest<EnableObjectLegalHoldArgs.Builder, EnableObjectLegalHoldArgs> {
    @Serial
    private static final long serialVersionUID = 2473860127578717425L;

    @Override
    public EnableObjectLegalHoldArgs.Builder getBuilder() {
        return EnableObjectLegalHoldArgs.builder();
    }
}
