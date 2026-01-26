package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.multipart.AbortMultipartUploadDomain;
import io.minio.AbortMultipartUploadResponse;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio AbortMultipartUploadResponse 转 AbortMultipartUploadDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:16
 */
public class AbortMultipartUploadResponseToDomainConverter implements Converter<AbortMultipartUploadResponse, AbortMultipartUploadDomain> {
    @Override
    public AbortMultipartUploadDomain convert(AbortMultipartUploadResponse source) {

        AbortMultipartUploadDomain domain = new AbortMultipartUploadDomain();
        domain.setUploadId(source.uploadId());
        domain.setBucketName(source.bucket());
        domain.setRegion(source.region());
        domain.setObjectName(source.object());
        return domain;
    }
}
