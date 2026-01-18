package com.ysmjjsy.goya.component.oss.core.domain.multipart;

import com.ysmjjsy.goya.component.oss.core.core.domain.OssDomain;
import com.ysmjjsy.goya.component.oss.core.domain.base.OwnerDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>分片上传列表返回条目域对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:31
 */
@Data
@Schema(name = "分片上传列表返回条目域对象", title = "分片上传列表返回条目域对象")
public class UploadDomain implements OssDomain {

    @Serial
    private static final long serialVersionUID = -7733000681023184216L;
    /**
     * 存储此upload的密钥
     */
    @Schema(name = "对象标记")
    private String key;

    /**
     * 此分片上传的唯一ID
     */
    @Schema(name = "上传ID")
    private String uploadId;

    /**
     * 此分片上传的拥有者
     */
    @Schema(name = "分片上传的拥有者")
    private OwnerDomain owner;

    /**
     * 此分片上传的发起者
     */
    @Schema(name = "分片上传的发起者")
    private OwnerDomain initiator;

    /**
     * 存储类，指示如何存储此分片上传中的数据.
     */
    @Schema(name = "存储类", description = "指示如何存储此分片上传中的数据")
    private String storageClass;

    /**
     * 启动此分片上传的时间
     */
    @Schema(name = "启动此分片上传的时间")
    private LocalDateTime initiated;
}
