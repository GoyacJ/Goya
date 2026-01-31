package com.ysmjjsy.goya.component.framework.security.dsl;

import com.ysmjjsy.goya.component.framework.security.domain.Resource;
import com.ysmjjsy.goya.component.framework.security.domain.Subject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * <p>范围过滤器上下文</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class RangeFilterContext implements Serializable {

    @Serial
    private static final long serialVersionUID = -2943762888453499206L;

    /**
     * 租户代码
     */
    @Schema(description = "租户代码")
    private String tenantCode;

    /**
     * 主体
     */
    @Schema(description = "主体")
    private Subject subject;

    /**
     * 资源
     */
    @Schema(description = "资源")
    private Resource resource;

    /**
     * 环境
     */
    @Schema(description = "环境")
    private Map<String, Object> environment;
}
