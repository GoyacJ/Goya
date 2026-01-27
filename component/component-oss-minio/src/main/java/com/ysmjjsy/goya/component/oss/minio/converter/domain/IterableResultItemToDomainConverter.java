package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.arguments.object.ListObjectsArguments;
import com.ysmjjsy.goya.component.oss.core.domain.object.ListObjectsDomain;
import com.ysmjjsy.goya.component.oss.core.domain.object.ObjectDomain;
import com.ysmjjsy.goya.component.oss.minio.utils.MinioConverterUtils;
import io.minio.Result;
import io.minio.messages.Item;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * <p>Iterable  > 转 BucketDomain 转换器   </p>
 *
 * @author goya
 * @since 2025/11/1 16:18
 */
public class IterableResultItemToDomainConverter implements Converter<Iterable<Result<Item>>, ListObjectsDomain> {

    private final String bucketName;

    private String prefix;
    private ListObjectsArguments listObjectsArguments;

    public IterableResultItemToDomainConverter(String bucketName) {
        this.bucketName = bucketName;
    }

    public IterableResultItemToDomainConverter(String bucketName, String prefix) {
        this.bucketName = bucketName;
        this.prefix = prefix;
    }

    public IterableResultItemToDomainConverter(ListObjectsArguments listObjectsArguments) {
        this.listObjectsArguments = listObjectsArguments;
        this.bucketName = listObjectsArguments.getBucketName();
        this.prefix = listObjectsArguments.getPrefix();
    }

    @Override
    public ListObjectsDomain convert(Iterable<Result<Item>> source) {

        List<ObjectDomain> objectDomains = MinioConverterUtils.toDomains(source, new ResultItemToDomainConverter(this.bucketName));

        ListObjectsDomain domain = new ListObjectsDomain();
        domain.setBucketName(this.bucketName);
        domain.setPrefix(this.prefix);

        if (ObjectUtils.isNotEmpty(listObjectsArguments)) {
            domain.setMarker(listObjectsArguments.getMarker());
            domain.setDelimiter(listObjectsArguments.getDelimiter());
            domain.setMaxKeys(listObjectsArguments.getMaxKeys());
            domain.setEncodingType(listObjectsArguments.getEncodingType());
            domain.setBucketName(listObjectsArguments.getBucketName());
        }

        domain.setSummaries(objectDomains);

        return domain;
    }
}