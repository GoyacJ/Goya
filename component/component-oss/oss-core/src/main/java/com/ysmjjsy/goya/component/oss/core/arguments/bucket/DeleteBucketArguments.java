package com.ysmjjsy.goya.component.oss.core.arguments.bucket;

import com.ysmjjsy.goya.component.oss.core.arguments.base.BucketArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>删除存储桶请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "删除存储桶请求参数实体", title = "删除存储桶请求参数实体")
public class DeleteBucketArguments extends BucketArguments {
    @Serial
    private static final long serialVersionUID = -3493259526191199729L;

}
