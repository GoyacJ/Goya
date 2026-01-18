package com.ysmjjsy.goya.component.oss.core.arguments.object;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>对象列表V2请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "对象列表V2请求参数实体", title = "对象列表V2请求参数实体")
public class ListObjectsV2Arguments extends ListObjectsArguments {

    @Serial
    private static final long serialVersionUID = 5859651296715897463L;

    @Schema(name = "允许从特定点继续列表", description = "ContinuationToken在截断的列表结果中提供")
    private String continuationToken;

    @Schema(name = "是否包括所有者字段", description = "默认情况下，ListObjectsV2结果中不存在所有者字段。如果此标志设置为true，则将包括所有者字段。")
    private Boolean fetchOwner = false;

    @Schema(name = "版本ID标记", description = "仅在 Minio GetObjectVersions情况下使用")
    private String versionIdMarker;
}
