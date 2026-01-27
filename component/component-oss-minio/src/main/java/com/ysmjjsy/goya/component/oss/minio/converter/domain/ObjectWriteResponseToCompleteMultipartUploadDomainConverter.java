package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.framework.oss.domain.multipart.CompleteMultipartUploadDomain;
import com.ysmjjsy.goya.component.oss.minio.definition.domain.ObjectWriteResponseToDomain;
import io.minio.ObjectWriteResponse;

/**
 * <p>Minio ObjectWriteResponse 转 CompleteMultipartUploadDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:20
 */
public class ObjectWriteResponseToCompleteMultipartUploadDomainConverter extends ObjectWriteResponseToDomain<CompleteMultipartUploadDomain> {
    @Override
    public CompleteMultipartUploadDomain getInstance(ObjectWriteResponse source) {
        return new CompleteMultipartUploadDomain();
    }
}