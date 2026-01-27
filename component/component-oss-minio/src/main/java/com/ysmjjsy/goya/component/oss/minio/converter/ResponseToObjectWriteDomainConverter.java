package com.ysmjjsy.goya.component.oss.minio.converter;

import com.ysmjjsy.goya.component.framework.oss.domain.base.ObjectWriteDomain;
import io.minio.ObjectWriteResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>ObjectWriteResponse 转 Entity 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:31
 */
public class ResponseToObjectWriteDomainConverter implements Converter<ObjectWriteResponse, ObjectWriteDomain> {
    @Override
    public ObjectWriteDomain convert(ObjectWriteResponse response) {
        if (ObjectUtils.isNotEmpty(response)) {
            ObjectWriteDomain domain = new ObjectWriteDomain();
            domain.setEtag(response.etag());
            domain.setVersionId(response.versionId());
            domain.setBucketName(response.bucket());
            domain.setRegion(response.region());
            domain.setObjectName(response.object());
            return domain;
        }

        return null;
    }
}