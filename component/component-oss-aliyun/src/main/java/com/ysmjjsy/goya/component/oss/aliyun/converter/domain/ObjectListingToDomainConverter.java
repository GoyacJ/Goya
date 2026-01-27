package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.ObjectListing;
import com.ysmjjsy.goya.component.framework.oss.domain.object.ListObjectsDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.object.ObjectDomain;
import com.ysmjjsy.goya.component.framework.oss.utils.OssConverterUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * <p>Aliyun ObjectListing 转 ListObjectsDomain 转换器 </p>
 *
 * @author goya
 * @since 2023/8/10 19:35
 */
public class ObjectListingToDomainConverter implements Converter<ObjectListing, ListObjectsDomain> {
    @Override
    public ListObjectsDomain convert(ObjectListing source) {

        List<ObjectDomain> summaries = OssConverterUtils.toDomains(source.getObjectSummaries(), new ObjectSummaryToDomainConverter(source.getDelimiter()));

        ListObjectsDomain domain = new ListObjectsDomain();
        domain.setSummaries(summaries);
        domain.setNextMarker(source.getNextMarker());
        domain.setIsTruncated(source.isTruncated());
        domain.setPrefix(source.getPrefix());
        domain.setMarker(source.getMarker());
        domain.setDelimiter(source.getDelimiter());
        domain.setMaxKeys(source.getMaxKeys());
        domain.setEncodingType(source.getEncodingType());
        domain.setBucketName(source.getBucketName());

        return domain;
    }
}
