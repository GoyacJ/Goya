package com.ysmjjsy.goya.component.oss.minio.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ysmjjsy.goya.component.oss.minio.converter.sse.RequestToServerSideEncryptionCustomerKeyConverter;
import io.minio.ObjectReadArgs;
import io.minio.ServerSideEncryptionCustomerKey;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

import java.io.Serial;

/**
 * <p> ObjectReadRequest </p>
 *
 * @author goya
 * @since 2023/5/30 23:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ObjectReadRequest<B extends ObjectReadArgs.Builder<B, A>, A extends ObjectReadArgs> extends ObjectVersionRequest<B, A> {

    @Serial
    private static final long serialVersionUID = 2665642685090499447L;

    private final Converter<String, ServerSideEncryptionCustomerKey> toCustomerKey = new RequestToServerSideEncryptionCustomerKeyConverter();

    @Schema(name = "服务端加密自定义Key", description = "Minio 默认仅支持 256 位 AES")
    private String customerKey;

    @JsonIgnore
    private ServerSideEncryptionCustomerKey serverSideEncryptionCustomerKey;

    public ServerSideEncryptionCustomerKey getServerSideEncryptionCustomerKey() {
        serverSideEncryptionCustomerKey = toCustomerKey.convert(getCustomerKey());
        return serverSideEncryptionCustomerKey;
    }

    @Override
    public void prepare(B builder) {

        if (ObjectUtils.isNotEmpty(getServerSideEncryptionCustomerKey())) {
            builder.ssec(getServerSideEncryptionCustomerKey());
        }

        super.prepare(builder);
    }
}
