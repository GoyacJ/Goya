package com.ysmjjsy.goya.component.oss.minio.definition.domain;

import com.ysmjjsy.goya.component.framework.oss.core.converter.OssConverter;
import com.ysmjjsy.goya.component.framework.oss.domain.base.BaseDomain;
import io.minio.GenericResponse;

/**
 * <p>Minio GenericResponse 转 统一定义 BaseDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:00
 */
public abstract class GenericResponseToDomainConverter<S extends GenericResponse, T extends BaseDomain> implements OssConverter<S, T> {

    @Override
    public void prepare(S source, T instance) {
        instance.setBucketName(source.bucket());
        instance.setRegion(source.region());
        instance.setObjectName(source.object());
    }
}