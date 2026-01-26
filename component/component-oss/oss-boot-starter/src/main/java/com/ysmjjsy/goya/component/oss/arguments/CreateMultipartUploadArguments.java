package com.ysmjjsy.goya.component.oss.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.base.ObjectArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>创建分片上传Dto</p>
 *
 * @author goya
 * @since 2025/11/3 09:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "创建分片上传请求参数实体", title = "创建分片上传请求参数实体")
public class CreateMultipartUploadArguments extends ObjectArguments {

    @Serial
    private static final long serialVersionUID = -7988476381297090939L;

    @Min(value = 1, message = "分片数量不能小于等于1")
    @Schema(name = "分片数量")
    private Integer partNumber;
}
