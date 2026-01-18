package com.ysmjjsy.goya.component.oss.minio.request.object;

import com.ysmjjsy.goya.component.oss.minio.converter.RequestToComposeSourceConverter;
import com.ysmjjsy.goya.component.oss.minio.definition.ObjectWriteRequest;
import com.ysmjjsy.goya.component.oss.minio.request.domain.ComposeSourceRequest;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.convert.converter.Converter;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * <p> 组合来自不同源对象请求参数 </p>
 *
 * @author goya
 * @since 2023/5/31 14:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ComposeObjectRequest extends ObjectWriteRequest<ComposeObjectArgs.Builder, ComposeObjectArgs> {

    @Serial
    private static final long serialVersionUID = -6393270667382963994L;

    private final Converter<ComposeSourceRequest, ComposeSource> requestTo = new RequestToComposeSourceConverter();
    private List<ComposeSourceRequest> sources;

    public ComposeObjectRequest(CopyObjectRequest request) {
        this.setExtraHeaders(request.getExtraHeaders());
        this.setExtraQueryParams(request.getExtraQueryParams());
        this.setBucketName(request.getBucketName());
        this.setRegion(request.getRegion());
        this.setObjectName(request.getObjectName());
        this.setHeaders(request.getHeaders());
        this.setUserMetadata(request.getUserMetadata());
        this.setServerSideEncryption(request.getServerSideEncryption());
        this.setTags(request.getTags());
        this.setRetention(request.getRetention());
        this.setLegalHold(request.getLegalHold());
        this.sources = new LinkedList<>();
        this.sources.add(new ComposeSourceRequest(request.getSource()));
    }

    @Override
    public void prepare(ComposeObjectArgs.Builder builder) {
        if (CollectionUtils.isNotEmpty(getSources())) {
            List<ComposeSource> composeSources = getSources().stream().map(requestTo::convert).toList();
            builder.sources(composeSources);
        }
        super.prepare(builder);
    }

    @Override
    public ComposeObjectArgs.Builder getBuilder() {
        return ComposeObjectArgs.builder();
    }
}
