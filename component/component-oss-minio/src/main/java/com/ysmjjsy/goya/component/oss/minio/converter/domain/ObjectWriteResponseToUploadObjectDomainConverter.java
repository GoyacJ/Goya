package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.object.UploadObjectDomain;
import com.ysmjjsy.goya.component.oss.minio.definition.domain.ObjectWriteResponseToDomain;
import io.minio.ObjectWriteResponse;

/**
 * <p>Minio ObjectWriteResponse 转 UploadObjectDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:20
 */
public class ObjectWriteResponseToUploadObjectDomainConverter extends ObjectWriteResponseToDomain<UploadObjectDomain> {
    @Override
    public UploadObjectDomain getInstance(ObjectWriteResponse source) {
        return new UploadObjectDomain();
    }
}