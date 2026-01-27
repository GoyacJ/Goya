package com.ysmjjsy.goya.component.framework.oss.domain.bucket;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ysmjjsy.goya.component.core.constants.DefaultConst;
import com.ysmjjsy.goya.component.oss.core.core.domain.OssDomain;
import com.ysmjjsy.goya.component.oss.core.domain.base.OwnerDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>统一存储桶域对象定义</p>
 *
 * @author goya
 * @since 2025/11/1 14:28
 */
@Data
@Schema(name = "存储桶")
public class BucketDomain implements OssDomain {

    @Serial
    private static final long serialVersionUID = 6639730317644142908L;

    /**
     * 存储桶名称
     */
    @Schema(name = "存储桶名称")
    private String bucketName;

    /**
     * 存储桶所有者信息
     */
    @Schema(name = "存储桶所有者信息", description = "Minio listBuckets API 返回的 Bucket 信息中不包含 OwnerDomain 信息")
    private OwnerDomain ownerAttribute;

    /**
     * 存储桶创建时间
     */
    @Schema(name = "存储桶创建时间")
    @JsonFormat(pattern = DefaultConst.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS)
    private LocalDateTime creationDate;
}
