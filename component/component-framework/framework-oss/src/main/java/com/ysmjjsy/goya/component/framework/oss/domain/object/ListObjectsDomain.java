package com.ysmjjsy.goya.component.framework.oss.domain.object;

import com.ysmjjsy.goya.component.oss.core.arguments.object.ListObjectsArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * <p>对象结果</p>
 *
 * @author goya
 * @since 2025/11/1 14:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "对象结果")
public class ListObjectsDomain extends ListObjectsArguments {

    @Serial
    private static final long serialVersionUID = -5958462622966899665L;
    
    @Schema(name = "对象列表")
    private List<ObjectDomain> summaries;

    /**
     * 用于请求下一页结果的标记-仅当isTruncated成员指示此对象列表被截断时才会填充
     */
    @Schema(name = "请求下一页结果的标记", description = "仅当isTruncated成员指示此对象列表被截断时才会填充")
    private String nextMarker;

    /**
     * 指示这是否是一个完整的列表，或者调用者是否需要向AmazonS3发出额外请求以查看S3 bucket的完整对象列表
     */
    @Schema(name = "否是一个完整的列表")
    private Boolean isTruncated;
}
