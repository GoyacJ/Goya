package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.framework.oss.domain.multipart.InitiateMultipartUploadDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;

/**
 * <p>CreateMultipartUploadResponse 转 InitiateMultipartUploadDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:09
 */
public class CreateMultipartUploadResponseToDomainConverter implements Converter<CreateMultipartUploadResponse, InitiateMultipartUploadDomain> {
    
    @Override
    public InitiateMultipartUploadDomain convert(CreateMultipartUploadResponse source) {
        InitiateMultipartUploadDomain domain = new InitiateMultipartUploadDomain();
        domain.setBucketName(source.bucket());
        domain.setObjectName(source.key());
        domain.setUploadId(source.uploadId());
        return domain;
    }
}
