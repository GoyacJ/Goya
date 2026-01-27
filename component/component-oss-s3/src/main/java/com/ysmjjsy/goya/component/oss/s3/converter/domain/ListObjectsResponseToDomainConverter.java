package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.framework.oss.domain.object.ListObjectsDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.object.ObjectDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>ListObjectsResponse 转 ListObjectsDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:20
 */
public class ListObjectsResponseToDomainConverter implements Converter<ListObjectsResponse, ListObjectsDomain> {

    private final Converter<S3Object, ObjectDomain> objectConverter = new S3ObjectToDomainConverter();

    @Override
    public ListObjectsDomain convert(ListObjectsResponse source) {
        ListObjectsDomain domain = new ListObjectsDomain();
        domain.setBucketName(source.name());
        domain.setPrefix(source.prefix());
        domain.setMarker(source.marker());
        domain.setDelimiter(source.delimiter());
        domain.setMaxKeys(source.maxKeys());
        
        if (source.encodingType() != null) {
            domain.setEncodingType(source.encodingType().toString());
        }
        
        domain.setNextMarker(source.nextMarker());
        domain.setIsTruncated(source.isTruncated());

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
