package com.ysmjjsy.goya.component.oss.core.domain.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>分片上传基础属性实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MultipartUploadDomain extends BaseDomain {

    @Serial
    private static final long serialVersionUID = -6224028927418280029L;

    @Schema(name = "上传ID")
    private String uploadId;
}
