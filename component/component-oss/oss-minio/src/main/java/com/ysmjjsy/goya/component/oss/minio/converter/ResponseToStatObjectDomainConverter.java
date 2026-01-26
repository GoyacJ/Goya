package com.ysmjjsy.goya.component.oss.minio.converter;

import com.ysmjjsy.goya.component.core.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.oss.minio.converter.retention.RetentionModeToEnumConverter;
import com.ysmjjsy.goya.component.oss.minio.domain.StatObjectDomain;
import com.ysmjjsy.goya.component.oss.minio.enums.RetentionModeEnums;
import io.minio.StatObjectResponse;
import io.minio.messages.RetentionMode;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio StatObjectResponse 转 StatObjectDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:31
 */
public class ResponseToStatObjectDomainConverter implements Converter<StatObjectResponse, StatObjectDomain> {

    private final Converter<RetentionMode, RetentionModeEnums> toRetentionModeEnums;

    public ResponseToStatObjectDomainConverter() {
        this.toRetentionModeEnums = new RetentionModeToEnumConverter();
    }

    @Override
    public StatObjectDomain convert(StatObjectResponse response) {

        StatObjectDomain domain = new StatObjectDomain();
        domain.setEtag(response.etag());
        domain.setSize(response.size());
        domain.setLastModified(GoyaDateUtils.zonedDateTimeToString(response.lastModified()));
        domain.setRetentionMode(toRetentionModeEnums.convert(response.retentionMode()));
        domain.setRetentionRetainUntilDate(GoyaDateUtils.zonedDateTimeToString(response.retentionRetainUntilDate()));
        domain.setLegalHold(response.legalHold().status());
        domain.setDeleteMarker(response.deleteMarker());
        domain.setUserMetadata(response.userMetadata());
        domain.setBucketName(response.bucket());
        domain.setRegion(response.region());
        domain.setObjectName(response.object());

        return domain;
    }
}