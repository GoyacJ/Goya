package com.ysmjjsy.goya.component.framework.oss.arguments.multipart;

import com.ysmjjsy.goya.component.framework.oss.arguments.base.BasePartArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>中止分片上传请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:46
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "中止分片上传请求参数实体", title = "中止分片上传请求参数实体")
public class AbortMultipartUploadArguments extends BasePartArguments {
    @Serial
    private static final long serialVersionUID = -4696513622267667362L;

}
