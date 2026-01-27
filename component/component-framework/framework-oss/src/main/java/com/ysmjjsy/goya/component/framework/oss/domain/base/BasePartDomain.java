package com.ysmjjsy.goya.component.framework.oss.domain.base;

import com.ysmjjsy.goya.component.framework.oss.core.domain.OssDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

/**
 * <p>分片域对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:25
 */
@Data
public abstract class BasePartDomain implements OssDomain {

    @Serial
    private static final long serialVersionUID = -8852645427323941933L;

    @Schema(name = "分片编号")
    private int partNumber;

    @Schema(name = "新对象的ETag值")
    private String etag;
}
