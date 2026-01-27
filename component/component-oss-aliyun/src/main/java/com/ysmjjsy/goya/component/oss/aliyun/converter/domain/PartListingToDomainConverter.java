package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.PartListing;
import com.aliyun.oss.model.PartSummary;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.ListPartsDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.PartSummaryDomain;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * <p>PartListing 转 ListPartsDomain 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 20:44
 */
public class PartListingToDomainConverter implements Converter<PartListing, ListPartsDomain> {

    private final Converter<List<PartSummary>, List<PartSummaryDomain>> toDomain = new PartSummaryToDomainConverter();

    @Override
    public ListPartsDomain convert(PartListing source) {

        ListPartsDomain domain = new ListPartsDomain();
        domain.setStorageClass(source.getStorageClass());
        domain.setMaxParts(source.getMaxParts());
        domain.setPartNumberMarker(source.getPartNumberMarker());
        domain.setNextPartNumberMarker(source.getNextPartNumberMarker());
        domain.setIsTruncated(source.isTruncated());
        domain.setParts(toDomain.convert(source.getParts()));
        domain.setUploadId(source.getUploadId());
        domain.setBucketName(source.getBucketName());
        domain.setObjectName(source.getKey());

        return domain;
    }
}
