package com.ysmjjsy.goya.component.oss.minio.domain;

import com.ysmjjsy.goya.component.framework.oss.domain.base.BaseDomain;
import com.ysmjjsy.goya.component.oss.minio.enums.RetentionModeEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Map;

/**
 * <p>对象信息</p>
 *
 * @author goya
 * @since 2025/11/1 15:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StatObjectDomain extends BaseDomain {

    @Serial
    private static final long serialVersionUID = 4486811763644545287L;

    @Schema(name = "ETag")
    private String etag;
    @Schema(name = "最后修改时间")
    private String lastModified;
    @Schema(name = "对象大小")
    private Long size;
    @Schema(name = "用户自定义元数据")
    private Map<String, String> userMetadata;
    @Schema(name = "保留模式")
    private RetentionModeEnums retentionMode;
    @Schema(name = "保留截止日期")
    private String retentionRetainUntilDate;
    @Schema(name = "是否合规持有")
    private Boolean legalHold;
    @Schema(name = "是否标记删除")
    private Boolean deleteMarker;
}
