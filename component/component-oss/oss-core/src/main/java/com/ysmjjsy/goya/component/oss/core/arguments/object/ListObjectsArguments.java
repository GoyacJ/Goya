package com.ysmjjsy.goya.component.oss.core.arguments.object;

import com.ysmjjsy.goya.component.oss.core.arguments.base.BucketArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>对象列表请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "对象列表请求参数实体", title = "对象列表请求参数实体")
public class ListObjectsArguments extends BucketArguments {

    @Serial
    private static final long serialVersionUID = 762733955340428222L;

    @Schema(name = "前缀")
    private String prefix;

    @Schema(name = "关键字", description = "ListObjectV2 版本中对应的名称为 startMarker, 这里为了方便统一使用 marker")
    private String marker;

    @Schema(name = "分隔符", description = "如果recursive为true，那么默认值为'', 否则默认值为'/'")
    private String delimiter = "";

    @Min(value = 1, message = "maxKeys 值不能小于 1")
    @Max(value = 1000, message = "maxKeys 值不能大于 1000")
    @Schema(name = "最大关键字数量", description = "关键字数量必须大于1，同时小于等于1000, 默认值 1000")
    private Integer maxKeys = 1000;

    @Schema(name = "encodingType")
    private String encodingType;

    @Schema(name = "是否递归", description = "该属性仅在 Minio 环境下使用")
    private Boolean recursive = false;
}
