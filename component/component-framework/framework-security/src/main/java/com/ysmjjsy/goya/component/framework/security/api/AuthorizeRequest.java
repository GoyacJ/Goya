package com.ysmjjsy.goya.component.framework.security.api;

import com.ysmjjsy.goya.component.framework.security.domain.Action;
import com.ysmjjsy.goya.component.framework.security.context.ResourceContext;
import com.ysmjjsy.goya.component.framework.security.context.SubjectContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>鉴权请求。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -6390423694112847510L;

    /**
     * 租户编码
     */
    @Schema(description = "租户编码")
    private String tenantCode;

    /**
     * 主体解析上下文
     */
    @Schema(description = "主体解析上下文")
    @NotNull
    private SubjectContext subjectContext;

    /**
     * 资源解析上下文
     */
    @Schema(description = "资源解析上下文")
    @NotNull
    private ResourceContext resourceContext;

    /**
     * 操作定义
     */
    @Schema(description = "操作定义")
    private Action action;

    @NotNull
    @NonNull
    @Schema(description = "请求时间")
    private LocalDateTime requestTime;

    /**
     * 环境信息
     */
    @Schema(description = "环境信息")
    private Map<String, Object> environment;
}
