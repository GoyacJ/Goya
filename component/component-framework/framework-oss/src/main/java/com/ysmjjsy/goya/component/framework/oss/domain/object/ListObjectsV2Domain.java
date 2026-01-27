package com.ysmjjsy.goya.component.framework.oss.domain.object;

import com.ysmjjsy.goya.component.framework.oss.arguments.object.ListObjectsV2Arguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * <p>V2对象列表结果</p>
 *
 * @author goya
 * @since 2025/11/1 14:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ListObjectsV2Domain extends ListObjectsV2Arguments {

    @Serial
    private static final long serialVersionUID = 3158833363439044957L;
    
    @Schema(name = "对象列表")
    private List<ObjectDomain> summaries;

    /**
     * 指示这是否是一个完整的列表，或者调用者是否需要向AmazonS3发出额外请求以查看S3 bucket的完整对象列表
     */
    @Schema(name = "否是一个完整的列表")
    private boolean isTruncated;

    /**
     * KeyCount是此请求返回的密钥数。KeyCount将始终小于或等于MaxKeys字段
     */
    @Schema(name = "Key 数量")
    private int keyCount;

    /**
     * 当 isTruncated为 true 时，发送 NextContinuationToken，这意味着存储桶中可以列出更多对象。请求亚马逊
     * 可以通过提供此NextContinuationToken来继续下一个列表
     */
    @Schema(name = "下一个列表标记")
    private String nextContinuationToken;
}
