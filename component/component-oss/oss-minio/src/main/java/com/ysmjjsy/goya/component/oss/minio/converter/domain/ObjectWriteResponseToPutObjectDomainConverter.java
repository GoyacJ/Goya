package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.object.PutObjectDomain;
import com.ysmjjsy.goya.component.oss.minio.definition.domain.ObjectWriteResponseToDomain;
import io.minio.ObjectWriteResponse;

/**
 * <p>Minio ObjectWriteResponse 转 PutObjectDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:20
 */
public class ObjectWriteResponseToPutObjectDomainConverter extends ObjectWriteResponseToDomain<PutObjectDomain> {
    @Override
    public PutObjectDomain getInstance(ObjectWriteResponse source) {
        return new PutObjectDomain();
    }
}
