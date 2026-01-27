package com.ysmjjsy.goya.component.framework.oss.arguments.object;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>下载对象请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:50
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DownloadObjectArguments extends GetObjectArguments {

    @Serial
    private static final long serialVersionUID = -8805537174722298894L;

    @Schema(name = "文件名", description = "服务器端完整的文件名，包括绝对路径和名称")
    @NotEmpty(message = "文件名不能为空")
    private String filename;

    @Schema(name = "是否覆盖", description = "该参数仅用于Minio Download方法，替代DownloadObjectArgs，以便于与其它Dialect保持一致")
    private Boolean overwrite = false;
}
