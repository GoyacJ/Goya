package com.ysmjjsy.goya.component.framework.oss.arguments.base;

import com.ysmjjsy.goya.component.framework.oss.core.arguments.OssArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.util.Map;

/**
 * <p>请求参数对象基础定义</p>
 *
 * @author goya
 * @since 2025/11/1 14:39
 */
@Data
public abstract class BaseArguments implements OssArguments {

    @Serial
    private static final long serialVersionUID = 7729299722133258308L;

    @Schema(name = "额外的请求头")
    private Map<String, String> extraHeaders;

    @Schema(name = "额外的Query参数")
    private Map<String, String> extraQueryParams;
}
