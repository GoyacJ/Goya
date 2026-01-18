package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.multipart.InitiateMultipartUploadDomain;
import io.minio.messages.InitiateMultipartUploadResult;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio InitiateMultipartUploadResult 转 InitiateMultipartUploadDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:18
 */
public class InitiateMultipartUploadResultToDomainConverter implements Converter<InitiateMultipartUploadResult, InitiateMultipartUploadDomain> {
    @Override
    public InitiateMultipartUploadDomain convert(InitiateMultipartUploadResult source) {

        InitiateMultipartUploadDomain domain = new InitiateMultipartUploadDomain();
        domain.setUploadId(source.uploadId());
        domain.setBucketName(source.bucketName());
        domain.setObjectName(source.objectName());
        return domain;
    }
}