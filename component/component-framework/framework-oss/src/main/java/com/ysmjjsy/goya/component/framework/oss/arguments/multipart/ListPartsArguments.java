package com.ysmjjsy.goya.component.framework.oss.arguments.multipart;

import com.ysmjjsy.goya.component.framework.oss.arguments.base.BasePartArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>分片列表请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "分片列表请求参数实体", title = "分片列表请求参数实体")
public class ListPartsArguments extends BasePartArguments {

    @Serial
    private static final long serialVersionUID = 8208820796126234725L;

    /**
     * 分片列表中要返回的最大分片数
     */
    @Schema(name = "最大分片数")
    private Integer maxParts;

    /**
     * 可选的分片号标记，指示要列出分片的结果中的位置
     */
    @Schema(name = "分片号标记")
    private Integer partNumberMarker;
}
