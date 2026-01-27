package com.ysmjjsy.goya.component.framework.oss.domain.multipart;

import com.ysmjjsy.goya.component.framework.oss.domain.base.MultipartUploadDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>初始化分片上传返回结果</p>
 *
 * @author goya
 * @since 2025/11/1 14:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "初始化分片上传返回结果", title = "初始化分片上传返回结果")
public class InitiateMultipartUploadDomain extends MultipartUploadDomain {

    @Serial
    private static final long serialVersionUID = -8099610412314050090L;
}
