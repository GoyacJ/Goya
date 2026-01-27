package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.framework.oss.domain.object.ListObjectsV2Domain;
import com.ysmjjsy.goya.component.framework.oss.domain.object.ObjectDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>ListObjectsV2Response 转 ListObjectsV2Domain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:30
 */
public class ListObjectsV2ResponseToDomainConverter implements Converter<ListObjectsV2Response, ListObjectsV2Domain> {

    private final Converter<S3Object, ObjectDomain> objectConverter = new S3ObjectToDomainConverter();

    @Override
    public ListObjectsV2Domain convert(ListObjectsV2Response source) {
        ListObjectsV2Domain domain = new ListObjectsV2Domain();
        domain.setBucketName(source.name());
        domain.setPrefix(source.prefix());
        domain.setDelimiter(source.delimiter());
        domain.setMaxKeys(source.maxKeys());
        domain.setContinuationToken(source.continuationToken());
        
        if (source.encodingType() != null) {
            domain.setEncodingType(source.encodingType().toString());
        }
        
        domain.setTruncated(source.isTruncated());
        domain.setKeyCount(source.keyCount());
        domain.setNextContinuationToken(source.nextContinuationToken());

        List<ObjectDomain> summaries = new ArrayList<>();
        
        if (source.contents() != null) {
            for (S3Object s3Object : source.contents()) {
                ObjectDomain objectDomain = objectConverter.convert(s3Object);
                objectDomain.setBucketName(source.name());
                summaries.add(objectDomain);
            }
        }
        
        if (source.commonPrefixes() != null) {
            for (CommonPrefix prefix : source.commonPrefixes()) {
                ObjectDomain prefixDomain = new ObjectDomain();
                prefixDomain.setBucketName(source.name());
                prefixDomain.setObjectName(prefix.prefix());
                prefixDomain.setIsDir(true);
                summaries.add(prefixDomain);
            }
        }
        
        domain.setSummaries(summaries);
        
        return domain;
    }
}
