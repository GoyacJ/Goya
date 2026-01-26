package com.ysmjjsy.goya.component.oss.minio.request.object;

import com.ysmjjsy.goya.component.oss.minio.definition.ObjectVersionRequest;
import io.minio.SetObjectTagsArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Map;

/**
 * <p> 设置对象标签请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/10 15:16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "设置对象标签请求参数实体", title = "设置对象标签请求参数实体")
public class SetObjectTagsRequest extends ObjectVersionRequest<SetObjectTagsArgs.Builder, SetObjectTagsArgs> {

    @Serial
    private static final long serialVersionUID = 1535639107826852196L;

    @Schema(name = "对象标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "对象标签不能为空")
    private Map<String, String> tags;

    @Override
    public void prepare(SetObjectTagsArgs.Builder builder) {
        builder.tags(getTags());
        super.prepare(builder);
    }

    @Override
    public SetObjectTagsArgs.Builder getBuilder() {
        return SetObjectTagsArgs.builder();
    }
}
