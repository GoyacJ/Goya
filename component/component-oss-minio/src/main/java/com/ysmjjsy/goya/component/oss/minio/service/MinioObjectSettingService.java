package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.oss.minio.bo.ObjectSettingBusiness;
import com.ysmjjsy.goya.component.oss.minio.converter.ResponseToStatObjectDomainConverter;
import com.ysmjjsy.goya.component.oss.minio.domain.StatObjectDomain;
import io.minio.StatObjectResponse;
import io.minio.messages.Tags;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

/**
 * <p> Object 管理页面数据获取 </p>
 *
 * @author goya
 * @since 2023/6/11 9:55
 */
@Service
public class MinioObjectSettingService {

    private final Converter<StatObjectResponse, StatObjectDomain> toStatObjectDomain;

    private final MinioObjectService minioObjectService;
    private final MinioObjectTagsService minioObjectTagsService;

    public MinioObjectSettingService(MinioObjectService minioObjectService, MinioObjectTagsService minioObjectTagsService) {
        this.minioObjectService = minioObjectService;
        this.minioObjectTagsService = minioObjectTagsService;
        this.toStatObjectDomain = new ResponseToStatObjectDomainConverter();
    }

    public ObjectSettingBusiness get(String bucketName, String region, String objectName) {
        StatObjectResponse statObjectResponse = minioObjectService.statObject(bucketName, region, objectName);
        StatObjectDomain statObjectDomain = toStatObjectDomain.convert(statObjectResponse);

        Tags tags = minioObjectTagsService.getObjectTags(bucketName, region, objectName);

        ObjectSettingBusiness business = new ObjectSettingBusiness();
        business.setTags(tags.get());
        business.setRetentionMode(statObjectDomain.getRetentionMode());
        business.setRetentionRetainUntilDate(statObjectDomain.getRetentionRetainUntilDate());
        business.setLegalHold(statObjectDomain.getLegalHold());
        business.setDeleteMarker(statObjectDomain.getDeleteMarker());
        business.setEtag(statObjectDomain.getEtag());
        business.setLastModified(statObjectDomain.getLastModified());
        business.setSize(statObjectDomain.getSize());
        business.setUserMetadata(statObjectDomain.getUserMetadata());

        return business;
    }
}
