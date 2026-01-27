package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.framework.oss.domain.object.ObjectMetadataDomain;
import io.minio.StatObjectResponse;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio StatObjectResponse 转 ObjectMetadataDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:24
 */
public class StatObjectResponseToDomainConverter implements Converter<StatObjectResponse, ObjectMetadataDomain> {
    @Override
    public ObjectMetadataDomain convert(StatObjectResponse source) {

        ObjectMetadataDomain domain = new ObjectMetadataDomain();
        domain.setUserMetadata(source.userMetadata());
        domain.setContentLength(source.size());
        domain.setContentType(source.contentType());
        domain.setLastModified(GoyaDateUtils.zonedDateTimeToLocalDateTime(source.lastModified()));
        domain.setEtag(source.etag());
        domain.setVersionId(source.versionId());
        domain.setBucketName(source.bucket());
        domain.setRegion(source.region());
        domain.setObjectName(source.object());

        return domain;
    }
}