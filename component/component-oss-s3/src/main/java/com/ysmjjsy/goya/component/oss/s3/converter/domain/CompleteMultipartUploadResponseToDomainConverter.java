package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.multipart.CompleteMultipartUploadDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;

/**
 * <p>CompleteMultipartUploadResponse 转 CompleteMultipartUploadDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 20:58
 */
public class CompleteMultipartUploadResponseToDomainConverter implements Converter<CompleteMultipartUploadResponse, CompleteMultipartUploadDomain> {
    
    @Override
    public CompleteMultipartUploadDomain convert(CompleteMultipartUploadResponse source) {
        CompleteMultipartUploadDomain domain = new CompleteMultipartUploadDomain();
        domain.setBucketName(source.bucket());
        domain.setObjectName(source.key());
        domain.setEtag(source.eTag());
        domain.setVersionId(source.versionId());
        return domain;
    }
}
