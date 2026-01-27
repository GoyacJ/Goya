package com.ysmjjsy.goya.component.oss.s3.repository;

import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.framework.oss.arguments.bucket.CreateBucketArguments;
import com.ysmjjsy.goya.component.framework.oss.arguments.bucket.DeleteBucketArguments;
import com.ysmjjsy.goya.component.framework.oss.core.repository.OssBucketRepository;
import com.ysmjjsy.goya.component.framework.oss.domain.bucket.BucketDomain;
import com.ysmjjsy.goya.component.framework.oss.utils.OssConverterUtils;
import com.ysmjjsy.goya.component.oss.s3.converter.domain.ListBucketResponseToDomainConverter;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;

/**
 * <p>Amazon S3 Java OSS API 存储桶操作实现 </p>
 *
 * @author goya
 * @since 2023/7/24 19:10
 */
@Slf4j
@Service
public class S3BucketRepository extends BaseS3Service implements OssBucketRepository {

    public S3BucketRepository(AbstractObjectPool<S3Client> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    @Override
    public boolean doesBucketExist(String bucketName) {
        S3Client client = getClient();
        try {
            client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            return true;
        } catch (NoSuchBucketException e) {
            log.warn("bucket no such bucketName:{}", bucketName, e);
            return false;
        } catch (S3Exception e) {
            log.warn("s3 bucket exception", e);
            // 403 也代表 bucket 存在，但你没权限
            if (e.statusCode() == 403) {
                return true;
            }
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public List<BucketDomain> listBuckets() {
        S3Client client = getClient();
        try {
            ListBucketsResponse response = client.listBuckets();
            return OssConverterUtils.toDomain(response, new ListBucketResponseToDomainConverter());
        } finally {
            close(client);
        }
    }

    @Override
    public BucketDomain createBucket(String bucketName) {
        CreateBucketArguments arguments = new CreateBucketArguments();
        arguments.setBucketName(bucketName);
        return createBucket(arguments);
    }

    @Override
    public BucketDomain createBucket(CreateBucketArguments arguments) {
        S3Client client = getClient();
        try {
            CreateBucketRequest.Builder builder = CreateBucketRequest.builder()
                    .bucket(arguments.getBucketName());

            // 非 us-east-1 必须指定 location
            if (arguments.getRegion() != null) {
                builder.createBucketConfiguration(
                        CreateBucketConfiguration.builder()
                                .locationConstraint(arguments.getRegion())
                                .build()
                );
            }

            client.createBucket(builder.build());

            BucketDomain bucketDomain = new BucketDomain();
            bucketDomain.setBucketName(arguments.getBucketName());
            return bucketDomain;
        } catch (BucketAlreadyOwnedByYouException e) {
            log.info("Bucket already owned by you: {}", arguments.getBucketName(), e);
            BucketDomain bucketDomain = new BucketDomain();
            bucketDomain.setBucketName(arguments.getBucketName());
            return bucketDomain;
        } finally {
            close(client);
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        DeleteBucketArguments deleteBucketArguments = new DeleteBucketArguments();
        deleteBucketArguments.setBucketName(bucketName);
        deleteBucket(deleteBucketArguments);
    }

    @Override
    public void deleteBucket(DeleteBucketArguments arguments) {
        S3Client client = getClient();
        try {
            client.deleteBucket(DeleteBucketRequest.builder()
                    .bucket(arguments.getBucketName())
                    .build());
        } finally {
            close(client);
        }
    }
}
