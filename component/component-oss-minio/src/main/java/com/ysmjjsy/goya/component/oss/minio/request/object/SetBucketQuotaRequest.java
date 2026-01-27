package com.ysmjjsy.goya.component.oss.minio.request.object;

import com.ysmjjsy.goya.component.framework.common.pojo.IEntity;
import com.ysmjjsy.goya.component.oss.minio.enums.QuotaUnitEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;

/**
 * <p> 设置存储桶配额请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/28 16:08
 */
@Data
@Schema(name = "设置存储桶配额请求参数实体", title = "设置存储桶配额请求参数实体")
public class SetBucketQuotaRequest implements IEntity {

    @Serial
    private static final long serialVersionUID = -4812704497659725748L;

    @Schema(name = "存储桶名称")
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    @Schema(name = "配额大小")
    @Min(value = 0, message = "配额大小不能小于 0")
    private Long size;

    @Schema(name = "配额单位", description = "配额单位目前支持 KB、MB、GB、TB")
    private QuotaUnitEnums unit = QuotaUnitEnums.MB;

}
