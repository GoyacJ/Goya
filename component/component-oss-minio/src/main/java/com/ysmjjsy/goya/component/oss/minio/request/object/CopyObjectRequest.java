package com.ysmjjsy.goya.component.oss.minio.request.object;

import com.ysmjjsy.goya.component.oss.minio.converter.RequestToCopySourceConverter;
import com.ysmjjsy.goya.component.oss.minio.definition.ObjectWriteRequest;
import com.ysmjjsy.goya.component.oss.minio.request.domain.CopySourceRequest;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.Directive;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

import java.io.Serial;

/**
 * <p> 拷贝对象 </p>
 *
 * @author goya
 * @since 2023/5/31 14:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CopyObjectRequest extends ObjectWriteRequest<CopyObjectArgs.Builder, CopyObjectArgs> {

    @Serial
    private static final long serialVersionUID = 5997286916860129939L;

    private final Converter<CopySourceRequest, CopySource> requestTo = new RequestToCopySourceConverter();

    @NotNull(message = "source 对象不能为空")
    private CopySourceRequest source;
    private String metadataDirective;
    private String taggingDirective;

    public CopyObjectRequest(ComposeObjectRequest request) {
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
        this.source = new CopySourceRequest(request.getSources().getFirst());
    }

    @Override
    public void prepare(CopyObjectArgs.Builder builder) {
        builder.source(requestTo.convert(getSource()));
        if (StringUtils.isNotBlank(getMetadataDirective())) {
            builder.metadataDirective(Directive.valueOf(getMetadataDirective()));
        }
        if (StringUtils.isNotBlank(getTaggingDirective())) {
            builder.taggingDirective(Directive.valueOf(getTaggingDirective()));
        }
        super.prepare(builder);
    }

    @Override
    public CopyObjectArgs.Builder getBuilder() {
        return CopyObjectArgs.builder();
    }
}
