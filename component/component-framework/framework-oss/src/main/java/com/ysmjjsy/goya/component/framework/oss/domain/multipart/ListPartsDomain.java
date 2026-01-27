package com.ysmjjsy.goya.component.framework.oss.domain.multipart;

import com.ysmjjsy.goya.component.framework.oss.domain.base.MultipartUploadDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.base.OwnerDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * <p>分片列表返回域对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:30
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ListPartsDomain extends MultipartUploadDomain {

    @Serial
    private static final long serialVersionUID = 1115907205807060413L;

    private OwnerDomain owner;

    private OwnerDomain initiator;

    private String storageClass;

    private Integer maxParts;

    private Integer partNumberMarker;

    private Integer nextPartNumberMarker;

    private Boolean isTruncated;

    private List<PartSummaryDomain> parts;
}
