package com.ysmjjsy.goya.component.oss.core.arguments.base;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;
import java.util.List;

/**
 * <p>基础的 Object Conditional Read 请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ObjectConditionalReadArguments extends ObjectReadArguments {

    @Serial
    private static final long serialVersionUID = -2613033483574247409L;

    @Schema(name = "offset")
    @DecimalMin(value = "0", message = "offset 参数不能小于 0")
    private Long offset;

    @Schema(name = "length")
    @DecimalMin(value = "0", message = "length 参数不能小于 0")
    private Long length;

    @Schema(name = "ETag值反向匹配约束列表")
    private List<String> notMatchEtag;

    @Schema(name = "ETag值匹配约束列表")
    private List<String> matchEtag;

    @Schema(name = "修改时间匹配约束")
    private Date modifiedSince;

    @Schema(name = "修改时间反向匹配约束")
    private Date unmodifiedSince;
}
