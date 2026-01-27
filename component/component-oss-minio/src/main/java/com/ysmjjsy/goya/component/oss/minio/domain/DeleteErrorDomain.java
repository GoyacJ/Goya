package com.ysmjjsy.goya.component.oss.minio.domain;

import com.ysmjjsy.goya.component.framework.oss.domain.object.DeleteObjectDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>Minio 错误信息基础实体</p>
 *
 * @author goya
 * @since 2025/11/1 15:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteErrorDomain extends DeleteObjectDomain {

    @Serial
    private static final long serialVersionUID = 1404925149511181662L;

    @Schema(name = "错误代码")
    private String code;
    @Schema(name = "错误信息")
    private String message;
    @Schema(name = "存储桶名称")
    private String bucketName;
    @Schema(name = "资源名称")
    private String resource;
    @Schema(name = "请求ID")
    private String requestId;
    @Schema(name = "主机ID")
    private String hostId;

}
