package com.ysmjjsy.goya.component.oss.minio.request.domain;

import com.ysmjjsy.goya.component.oss.minio.definition.ObjectConditionalReadRequest;
import io.minio.ComposeSource;
import io.minio.CopySource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Map;

/**
 * <p> 组合对象源请求参数 </p>
 *
 * @author goya
 * @since 2023/5/31 14:37
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "组合对象源请求参数")
public class ComposeSourceRequest extends ObjectConditionalReadRequest<ComposeSource.Builder, ComposeSource> {

    @Serial
    private static final long serialVersionUID = 378218461790723034L;

    private Long objectSize = null;
    private Map<String, String> headers = null;

    public ComposeSourceRequest(ObjectConditionalReadRequest<CopySource.Builder, CopySource> request) {
        this.setExtraHeaders(request.getExtraHeaders());
        this.setExtraQueryParams(request.getExtraQueryParams());
        this.setBucketName(request.getBucketName());
        this.setRegion(request.getRegion());
        this.setObjectName(request.getObjectName());
        this.setVersionId(request.getVersionId());
        this.setServerSideEncryptionCustomerKey(request.getServerSideEncryptionCustomerKey());
        this.setOffset(request.getOffset());
        this.setLength(request.getLength());
        this.setMatchETag(request.getMatchETag());
        this.setNotMatchETag(request.getNotMatchETag());
        this.setModifiedSince(request.getModifiedSince());
        this.setUnmodifiedSince(request.getUnmodifiedSince());
    }

    @Override
    public ComposeSource.Builder getBuilder() {
        return ComposeSource.builder();
    }
}
