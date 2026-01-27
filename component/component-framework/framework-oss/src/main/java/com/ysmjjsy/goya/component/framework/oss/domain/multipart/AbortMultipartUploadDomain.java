package com.ysmjjsy.goya.component.framework.oss.domain.multipart;

import com.ysmjjsy.goya.component.framework.oss.domain.base.MultipartUploadDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> 中止分片上传返回结果</p>
 *
 * @author goya
 * @since 2025/11/1 14:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "中止分片上传返回结果", title = "中止分片上传返回结果")
public class AbortMultipartUploadDomain extends MultipartUploadDomain {
    @Serial
    private static final long serialVersionUID = 3700122143516537658L;
}
