package com.ysmjjsy.goya.component.oss.minio.definition;

import io.minio.BaseArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;

import java.io.Serial;
import java.util.Map;

/**
 * <p> Minio 基础 Dto </p>
 *
 * @author goya
 * @since 2022/7/1 23:39
 */
@Data
public abstract class BaseRequest<B extends BaseArgs.Builder<B, A>, A extends BaseArgs> implements MinioRequestBuilder<B, A> {

    @Serial
    private static final long serialVersionUID = 3427613414644403845L;

    @Schema(name = "额外的请求头")
    private Map<String, String> extraHeaders;

    @Schema(name = "额外的Query参数")
    private Map<String, String> extraQueryParams;

    @Override
    public void prepare(B builder) {
        if (MapUtils.isNotEmpty(getExtraHeaders())) {
            builder.extraHeaders(getExtraHeaders());
        }

        if (MapUtils.isNotEmpty(getExtraQueryParams())) {
            builder.extraHeaders(getExtraQueryParams());
        }
    }

}
