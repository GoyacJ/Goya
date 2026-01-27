package com.ysmjjsy.goya.component.framework.oss.arguments.object;

import com.ysmjjsy.goya.component.framework.oss.arguments.base.BucketArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * <p>批量删除对象请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "批量删除对象请求参数实体", title = "批量删除对象请求参数实体")
public class DeleteObjectsArguments extends BucketArguments {

    @Serial
    private static final long serialVersionUID = 788013147291334479L;

    @Schema(name = "使用治理模式进行删除", description = "Minio 专用参数")
    private Boolean bypassGovernanceMode;

    @NotEmpty(message = "删除对象不能为空")
    private List<DeletedObjectArguments> objects;

    private Boolean quiet = false;
}
