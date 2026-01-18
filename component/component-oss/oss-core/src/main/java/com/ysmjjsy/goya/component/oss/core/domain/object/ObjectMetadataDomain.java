package com.ysmjjsy.goya.component.oss.core.domain.object;

import com.ysmjjsy.goya.component.oss.core.domain.base.ObjectWriteDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>下载对象返回结果域对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:34
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ObjectMetadataDomain extends ObjectWriteDomain {

    @Serial
    private static final long serialVersionUID = 5735164421471373920L;

    @Schema(name = "用户自定义 Metadata")
    private Map<String, String> userMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Schema(name = "内容大小")
    private long contentLength;

    @Schema(name = "contentType")
    private String contentType;

    @Schema(name = "最后修改时间")
    private LocalDateTime lastModified;
}
