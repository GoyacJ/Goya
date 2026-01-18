package com.ysmjjsy.goya.component.oss.minio.request.object;

import com.ysmjjsy.goya.component.oss.minio.definition.ObjectVersionRequest;
import io.minio.GetObjectRetentionArgs;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> GetObjectRetentionRequest </p>
 *
 * @author goya
 * @since 2023/4/18 16:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetObjectRetentionRequest extends ObjectVersionRequest<GetObjectRetentionArgs.Builder, GetObjectRetentionArgs> {
    @Serial
    private static final long serialVersionUID = 479063387480076456L;

    @Override
    public GetObjectRetentionArgs.Builder getBuilder() {
        return GetObjectRetentionArgs.builder();
    }
}
