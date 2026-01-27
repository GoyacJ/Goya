package com.ysmjjsy.goya.component.framework.oss.domain.multipart;

import com.ysmjjsy.goya.component.oss.core.domain.base.ObjectWriteDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>完成分片上传返回结果</p>
 *
 * @author goya
 * @since 2025/11/1 14:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "完成分片上传返回结果", title = "完成分片上传返回结果")
public class CompleteMultipartUploadDomain extends ObjectWriteDomain {

    @Serial
    private static final long serialVersionUID = -1421664977848635303L;
}