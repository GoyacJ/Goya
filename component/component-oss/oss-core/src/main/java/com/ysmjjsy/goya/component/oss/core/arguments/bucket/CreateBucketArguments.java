package com.ysmjjsy.goya.component.oss.core.arguments.bucket;

import com.ysmjjsy.goya.component.oss.core.arguments.base.BucketArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>创建存储桶请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "创建存储桶请求参数实体", title = "创建存储桶请求参数实体")
public class CreateBucketArguments extends BucketArguments {

    @Serial
    private static final long serialVersionUID = 4403002248245249736L;

    @Schema(name = "开启对象锁定", description = "仅在Minio环境下使用")
    private Boolean objectLock;
}
