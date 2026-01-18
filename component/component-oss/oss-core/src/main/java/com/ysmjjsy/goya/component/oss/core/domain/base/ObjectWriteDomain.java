package com.ysmjjsy.goya.component.oss.core.domain.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>ObjectWriteDomain</p>
 *
 * @author goya
 * @since 2025/11/1 14:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ObjectWriteDomain extends BaseDomain {

    @Serial
    private static final long serialVersionUID = -223665538024827372L;

    @Schema(name = "ETag 值")
    private String etag;

    @Schema(name = "版本ID")
    private String versionId;
}
