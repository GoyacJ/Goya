package com.ysmjjsy.goya.component.oss.core.arguments.object;

import com.ysmjjsy.goya.component.oss.core.arguments.base.ObjectVersionArguments;
import com.ysmjjsy.goya.component.oss.core.enums.HttpMethodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.Duration;

/**
 * <p>生成预签名URL请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:50
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "生成预签名URL请求参数实体", title = "生成预签名URL请求参数实体")
public class GeneratePresignedUrlArguments extends ObjectVersionArguments {

    @Serial
    private static final long serialVersionUID = -6335619439609253288L;
    @Schema(name = "对象保留模式", title = "存储模式的值只能是大写 GOVERNANCE 或者 COMPLIANCE")
    private HttpMethodEnum method = HttpMethodEnum.PUT;

    @Schema(name = "过期时间", type = "integer", title = "单位为秒，默认值为 7 天")
    private Duration expiration = Duration.ofDays(7);

    /**
     * Content-Type to url sign
     */
    private String contentType;

    /**
     * Content-MD5
     */
    private String contentMd5;
}
