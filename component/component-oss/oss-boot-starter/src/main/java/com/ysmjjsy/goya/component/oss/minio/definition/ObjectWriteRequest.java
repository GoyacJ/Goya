package com.ysmjjsy.goya.component.oss.minio.definition;

import com.ysmjjsy.goya.component.oss.minio.converter.retention.DomainToRetentionConverter;
import com.ysmjjsy.goya.component.oss.minio.converter.sse.RequestToServerSideEncryptionConverter;
import com.ysmjjsy.goya.component.oss.minio.domain.RetentionDomain;
import com.ysmjjsy.goya.component.oss.minio.domain.ServerSideEncryptionDomain;
import io.minio.ObjectWriteArgs;
import io.minio.ServerSideEncryption;
import io.minio.messages.Retention;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

import java.io.Serial;
import java.util.Map;

/**
 * <p> ObjectWriteRequest </p>
 *
 * @author goya
 * @since 2022/7/2 21:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ObjectWriteRequest<B extends ObjectWriteArgs.Builder<B, A>, A extends ObjectWriteArgs> extends ObjectRequest<B, A> {

    @Serial
    private static final long serialVersionUID = 5899460181935877081L;

    private final Converter<RetentionDomain, Retention> toRetention = new DomainToRetentionConverter();
    private final Converter<ServerSideEncryptionDomain, ServerSideEncryption> toServerSideEncryption = new RequestToServerSideEncryptionConverter();

    @Schema(name = "自定义 Header 信息")
    private Map<String, String> headers;

    @Schema(name = "用户信息")
    private Map<String, String> userMetadata;

    @Schema(name = "服务端加密")
    private ServerSideEncryptionDomain serverSideEncryption;

    @Schema(name = "标签")
    private Map<String, String> tags;

    @Schema(name = "保留配置")
    private RetentionDomain retention;

    @Schema(name = "合法持有")
    private Boolean legalHold;

    @Override
    public void prepare(B builder) {
        if (MapUtils.isNotEmpty(getHeaders())) {
            builder.headers(getHeaders());
        }

        if (MapUtils.isNotEmpty(getUserMetadata())) {
            builder.headers(getUserMetadata());
        }

        if (MapUtils.isNotEmpty(getTags())) {
            builder.headers(getTags());
        }

        builder.tags(getTags());

        if (ObjectUtils.isNotEmpty(getServerSideEncryption())) {
            ServerSideEncryption encryption = toServerSideEncryption.convert(getServerSideEncryption());
            if (ObjectUtils.isNotEmpty(encryption)) {
                builder.sse(encryption);
            }
        }

        if (ObjectUtils.isNotEmpty(getRetention())) {
            builder.retention(toRetention.convert(getRetention()));
        }

        if (ObjectUtils.isNotEmpty(getLegalHold())) {
            builder.legalHold(getLegalHold());
        }

        super.prepare(builder);
    }
}
