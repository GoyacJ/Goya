package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.bucket.BucketDomain;
import io.minio.messages.Bucket;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * <p>Bucket 转 BucketDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:16
 */
public class BucketToDomainConverter implements Converter<Bucket, BucketDomain> {

    @Override
    public BucketDomain convert(Bucket source) {

        Optional<Bucket> optional = Optional.ofNullable(source);
        return optional.map(bucket -> {
            BucketDomain domain = new BucketDomain();
            domain.setBucketName(bucket.name());
            Optional.ofNullable(bucket.creationDate()).ifPresent(zonedDateTime ->
                    domain.setCreationDate(LocalDateTime.now())
            );
            return domain;
        }).orElse(null);
    }
}
