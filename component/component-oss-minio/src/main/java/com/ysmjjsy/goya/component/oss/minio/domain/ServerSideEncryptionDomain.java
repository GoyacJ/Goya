package com.ysmjjsy.goya.component.oss.minio.domain;

import com.ysmjjsy.goya.component.framework.common.pojo.IEntity;
import com.ysmjjsy.goya.component.oss.minio.enums.ServerSideEncryptionEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.util.Map;

/**
 * <p>服务端加密域对象</p>
 *
 * @author goya
 * @since 2025/11/1 15:51
 */
@Data
public class ServerSideEncryptionDomain implements IEntity {

    @Serial
    private static final long serialVersionUID = 2431616659744573512L;

    @Schema(name = "服务端加密方式类型", description = "1:SSE_KMS, 2:SSE_S3, 3: 自定义")
    private ServerSideEncryptionEnums type;

    @Schema(name = "自定义服务端加密方式加密Key", description = "Minio 默认仅支持 256 位 AES")
    private String customerKey;

    @Schema(name = "KMS加密MasterKeyId", description = "可选参数，主要用于AWS_KMS加密算法")
    private String keyId;

    @Schema(name = "KMS加密context", description = "可选参数，主要用于AWS_KMS加密算法")
    private Map<String, String> context;
}
