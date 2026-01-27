package com.ysmjjsy.goya.component.framework.oss.arguments.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Map;

/**
 * <p>基础 Object Write 请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ObjectWriteArguments extends ObjectArguments {

    @Serial
    private static final long serialVersionUID = -676465955278302115L;

    @Schema(name = "请求头信息")
    private Map<String, String> requestHeaders;

    @Schema(name = "对象元数据")
    private Map<String, String> metadata;
}
