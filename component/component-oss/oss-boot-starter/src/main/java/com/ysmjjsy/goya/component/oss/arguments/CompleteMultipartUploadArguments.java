package com.ysmjjsy.goya.component.oss.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.base.BasePartArguments;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;

/**
 * <p>完成分片上传 Dto</p>
 *
 * @author goya
 * @since 2025/11/3 09:43
 */
@Schema(name = "完成分片上传请求参数实体", title = "完成分片上传请求参数实体")
public class CompleteMultipartUploadArguments extends BasePartArguments {

    @Serial
    private static final long serialVersionUID = -4959676152855401243L;
}
