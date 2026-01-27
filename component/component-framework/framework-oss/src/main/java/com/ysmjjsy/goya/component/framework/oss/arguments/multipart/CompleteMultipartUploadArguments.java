package com.ysmjjsy.goya.component.framework.oss.arguments.multipart;

import com.ysmjjsy.goya.component.oss.core.arguments.base.BasePartArguments;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.PartSummaryDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * <p>完成分片上传请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:46
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "完成分片上传请求参数实体", title = "完成分片上传请求参数实体")
public class CompleteMultipartUploadArguments extends BasePartArguments {

    @Serial
    private static final long serialVersionUID = 5228718647977237953L;

    @Schema(name = "分片列表不能为空")
    @NotEmpty(message = "分片列表不能为空")
    private List<PartSummaryDomain> parts;
}
