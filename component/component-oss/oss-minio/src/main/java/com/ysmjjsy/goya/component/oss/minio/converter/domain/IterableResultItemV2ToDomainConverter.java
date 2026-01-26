package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.arguments.object.ListObjectsV2Arguments;
import com.ysmjjsy.goya.component.oss.core.domain.object.ListObjectsV2Domain;
import com.ysmjjsy.goya.component.oss.core.domain.object.ObjectDomain;
import com.ysmjjsy.goya.component.oss.minio.utils.MinioConverterUtils;
import io.minio.Result;
import io.minio.messages.Item;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * <p>Iterable  > V2 转 BucketDomain 转换器   </p>
 *
 * @author goya
 * @since 2025/11/1 16:18
 */
public class IterableResultItemV2ToDomainConverter implements Converter<Iterable<Result<Item>>, ListObjectsV2Domain> {

    private final String bucketName;

    private String prefix;
    private ListObjectsV2Arguments listObjectsV2Arguments;

    public IterableResultItemV2ToDomainConverter(String bucketName) {
        this.bucketName = bucketName;
    }

    public IterableResultItemV2ToDomainConverter(String bucketName, String prefix) {
        this.bucketName = bucketName;
        this.prefix = prefix;
    }

    public IterableResultItemV2ToDomainConverter(ListObjectsV2Arguments listObjectsV2Arguments) {
        this.listObjectsV2Arguments = listObjectsV2Arguments;
        this.bucketName = listObjectsV2Arguments.getBucketName();
        this.prefix = listObjectsV2Arguments.getPrefix();
    }

    @Override
    public ListObjectsV2Domain convert(Iterable<Result<Item>> source) {

        List<ObjectDomain> objectDomains = MinioConverterUtils.toDomains(source, new ResultItemToDomainConverter(this.bucketName));

        ListObjectsV2Domain domain = new ListObjectsV2Domain();
        domain.setBucketName(this.bucketName);
        domain.setPrefix(this.prefix);

        if (ObjectUtils.isNotEmpty(listObjectsV2Arguments)) {
            domain.setMarker(listObjectsV2Arguments.getMarker());
            domain.setDelimiter(listObjectsV2Arguments.getDelimiter());
            domain.setMaxKeys(listObjectsV2Arguments.getMaxKeys());
            domain.setEncodingType(listObjectsV2Arguments.getEncodingType());
            domain.setBucketName(listObjectsV2Arguments.getBucketName());
        }

        domain.setSummaries(objectDomains);

        return domain;
    }
}
