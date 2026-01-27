package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.Bucket;
import com.ysmjjsy.goya.component.framework.oss.domain.base.OwnerDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.bucket.BucketDomain;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.Optional;

/**
 * <p>Aliyun Bucket 转 BucketDomain 转换器  </p>
 *
 * @author goya
 * @since 2023/7/27 16:29
 */
public class BucketToDomainConverter implements Converter<Bucket, BucketDomain> {
    @Override
    public BucketDomain convert(Bucket source) {

        Optional<Bucket> optional = Optional.ofNullable(source);
        return optional.map(bucket -> {

            BucketDomain bucketDomain = new BucketDomain();

            Optional.ofNullable(bucket.getOwner()).ifPresent(o -> {

                OwnerDomain ownerAttributeDomain = new OwnerDomain();
                ownerAttributeDomain.setId(bucket.getOwner().getId());
                ownerAttributeDomain.setDisplayName(bucket.getOwner().getDisplayName());
                bucketDomain.setOwnerAttribute(ownerAttributeDomain);
            });

            bucketDomain.setBucketName(bucket.getName());
            bucketDomain.setCreationDate(DateUtils.toLocalDateTime(bucket.getCreationDate()));

            return bucketDomain;
        }).orElse(null);
    }
}
