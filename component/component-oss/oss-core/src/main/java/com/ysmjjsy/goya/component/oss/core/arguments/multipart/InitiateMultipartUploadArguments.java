package com.ysmjjsy.goya.component.oss.core.arguments.multipart;

import com.ysmjjsy.goya.component.oss.core.arguments.base.ObjectArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>创建分片上传请求参数</p>
 *
 * @author goya
 * @since 2025/11/1 14:46
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "创建分片上传请求参数实体", title = "创建分片上传请求参数实体")
public class InitiateMultipartUploadArguments extends ObjectArguments {
    @Serial
    private static final long serialVersionUID = -4245667610244991885L;
}

