package com.ysmjjsy.goya.component.oss.core.domain.base;

import com.ysmjjsy.goya.component.oss.core.core.domain.OssDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>共性属性</p>
 *
 * @author goya
 * @since 2025/11/1 14:24
 */
@Data
public abstract class BaseDomain implements OssDomain {

    @Serial
    private static final long serialVersionUID = 902823179765112849L;

    @NotBlank(message = "存储桶名称不能为空")
    @Schema(name = "存储桶名称")
    private String bucketName;

    @Schema(name = "存储区域")
    private String region;

    @NotBlank(message = "对象名称不能为空")
    @Schema(name = "对象名称")
    private String objectName;

    @Schema(name = "用户自定义头信息")
    private Map<String, Object> header = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
}
