package com.ysmjjsy.goya.component.oss.core.arguments.object;

import com.ysmjjsy.goya.component.oss.core.arguments.base.PutObjectBaseArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>上传对象请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UploadObjectArguments extends PutObjectBaseArguments {

    @Serial
    private static final long serialVersionUID = -8201798745778370875L;

    @Schema(name = "文件名", description = "服务器端完整的文件名，包括绝对路径和名称")
    @NotEmpty(message = "文件名不能为空")
    private String filename;
}
