package com.ysmjjsy.goya.component.framework.oss.domain.object;

import com.ysmjjsy.goya.component.framework.oss.domain.bucket.BucketDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:34
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "对象")
public class ObjectDomain extends BucketDomain {

    @Serial
    private static final long serialVersionUID = 2019698406423977606L;

    /**
     * 存储此对象的密钥
     */
    @Schema(name = "存储此对象的密钥")
    private String objectName;
    /**
     * ETag。此对象内容的十六进制编码MD5哈希
     */
    @Schema(name = "ETag", description = "此对象内容的十六进制编码MD5哈希")
    private String eTag;
    /**
     * 此对象的大小，以字节为单位
     */
    @Schema(name = "对象大小", description = "以字节为单位")
    private Long size;
    /**
     * 对象最后一次被修改的日期
     */
    @Schema(name = "对象最后一次被修改的日期")
    private LocalDateTime lastModified;
    /**
     * 存储此对象的存储类
     */
    @Schema(name = "存储此对象的存储类")
    private String storageClass;

    @Schema(name = "是否为文件夹")
    private Boolean isDir;
}
