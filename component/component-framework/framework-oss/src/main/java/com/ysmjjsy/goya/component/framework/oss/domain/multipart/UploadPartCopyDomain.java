package com.ysmjjsy.goya.component.framework.oss.domain.multipart;

import com.ysmjjsy.goya.component.framework.oss.domain.base.PartDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>分片上传拷贝返回结果域对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:31
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "分片上传拷贝返回结果域对象", title = "分片上传拷贝返回结果域对象")
public class UploadPartCopyDomain extends PartDomain {

    @Serial
    private static final long serialVersionUID = 8719334659044076597L;

    @Schema(name = "分片上传ID")
    private String uploadId;
}
