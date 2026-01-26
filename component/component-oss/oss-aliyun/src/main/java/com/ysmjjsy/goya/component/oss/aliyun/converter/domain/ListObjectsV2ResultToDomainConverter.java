package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.ListObjectsV2Result;
import com.ysmjjsy.goya.component.oss.core.domain.object.ListObjectsV2Domain;
import com.ysmjjsy.goya.component.oss.core.domain.object.ObjectDomain;
import com.ysmjjsy.goya.component.oss.core.utils.OssConverterUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * <p>Aliyun ListObjectsV2Result 转 ListObjectsV2Domain 转换器 </p>
 *
 * @author goya
 * @since 2023/8/10 19:35
 */
public class ListObjectsV2ResultToDomainConverter implements Converter<ListObjectsV2Result, ListObjectsV2Domain> {
    @Override
    public ListObjectsV2Domain convert(ListObjectsV2Result source) {

        List<ObjectDomain> summaries = OssConverterUtils.toDomains(source.getObjectSummaries(), new ObjectSummaryToDomainConverter(source.getDelimiter()));

        ListObjectsV2Domain domain = new ListObjectsV2Domain();
        domain.setSummaries(summaries);
        domain.setTruncated(source.isTruncated());
        domain.setKeyCount(source.getKeyCount());
        domain.setNextContinuationToken(source.getNextContinuationToken());
        domain.setContinuationToken(source.getContinuationToken());
        domain.setPrefix(source.getPrefix());
        domain.setMarker(source.getStartAfter());
        domain.setDelimiter(source.getDelimiter());
        domain.setMaxKeys(source.getMaxKeys());
        domain.setEncodingType(source.getEncodingType());
        domain.setBucketName(source.getBucketName());


        return domain;
    }
}
