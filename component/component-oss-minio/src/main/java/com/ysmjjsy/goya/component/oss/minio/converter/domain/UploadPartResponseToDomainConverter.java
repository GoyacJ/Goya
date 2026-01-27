package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.multipart.UploadPartDomain;
import io.minio.UploadPartResponse;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio InitiateMultipartUploadResult 转 InitiateMultipartUploadDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:26
 */
public class UploadPartResponseToDomainConverter implements Converter<UploadPartResponse, UploadPartDomain> {

    @Override
    public UploadPartDomain convert(UploadPartResponse source) {
        UploadPartDomain domain = new UploadPartDomain();
        domain.setPartNumber(source.partNumber());
        domain.setEtag(source.etag());
        return domain;
    }
}