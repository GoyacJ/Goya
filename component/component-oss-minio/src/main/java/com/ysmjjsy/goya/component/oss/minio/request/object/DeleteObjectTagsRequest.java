package com.ysmjjsy.goya.component.oss.minio.request.object;

import com.ysmjjsy.goya.component.oss.minio.definition.ObjectVersionRequest;
import io.minio.DeleteObjectTagsArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> 删除对象标签请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/10 15:20
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "删除对象标签请求参数实体", title = "删除对象桶标签请求参数实体")
public class DeleteObjectTagsRequest extends ObjectVersionRequest<DeleteObjectTagsArgs.Builder, DeleteObjectTagsArgs> {
    @Serial
    private static final long serialVersionUID = 4179481621109726248L;

    @Override
    public DeleteObjectTagsArgs.Builder getBuilder() {
        return DeleteObjectTagsArgs.builder();
    }
}
