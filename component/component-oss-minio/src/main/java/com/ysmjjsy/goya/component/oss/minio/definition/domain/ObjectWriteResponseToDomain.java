package com.ysmjjsy.goya.component.oss.minio.definition.domain;

import com.ysmjjsy.goya.component.oss.core.domain.base.ObjectWriteDomain;
import io.minio.ObjectWriteResponse;

/**
 * <p>Minio ObjectWriteResponse 转 统一定义 ObjectWriteDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:00
 */
public abstract class ObjectWriteResponseToDomain<T extends ObjectWriteDomain> extends GenericResponseToDomainConverter<ObjectWriteResponse, T> {

    @Override
    public void prepare(ObjectWriteResponse response, T domain) {
        domain.setEtag(response.etag());
        domain.setVersionId(response.versionId());
        super.prepare(response, domain);
    }
}