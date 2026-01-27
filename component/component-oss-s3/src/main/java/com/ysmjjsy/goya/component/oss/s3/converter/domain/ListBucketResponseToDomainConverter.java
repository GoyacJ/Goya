package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.google.common.collect.Lists;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.framework.oss.domain.base.OwnerDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.bucket.BucketDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.List;
import java.util.Optional;

/**
 * <p>S3 Bucket 转 BucketDomain 转换器 </p>
 *
 * @author goya
 * @since 2023/7/15 21:28
 */
public class ListBucketResponseToDomainConverter implements Converter<ListBucketsResponse, List<BucketDomain>> {
    @Override
    public List<BucketDomain> convert(ListBucketsResponse source) {
        Optional<ListBucketsResponse> optional = Optional.ofNullable(source);
        return optional.map(response -> {
            OwnerDomain ownerAttributeDomain = new OwnerDomain();
            Optional.ofNullable(response.owner()).ifPresent(o -> {
                ownerAttributeDomain.setId(response.owner().id());
                ownerAttributeDomain.setDisplayName(response.owner().displayName());
            });

            List<Bucket> buckets = response.buckets();
            return buckets.stream().map(bucket -> {
                BucketDomain bucketDomain = new BucketDomain();
                bucketDomain.setBucketName(bucket.name());
                bucketDomain.setCreationDate(GoyaDateUtils.toLocalDateTime(bucket.creationDate()));
                return bucketDomain;
            }).toList();
        }).orElse(Lists.newArrayList());
    }
}
