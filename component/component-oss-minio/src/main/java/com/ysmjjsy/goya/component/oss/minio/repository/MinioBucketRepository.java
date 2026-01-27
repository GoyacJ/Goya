package com.ysmjjsy.goya.component.oss.minio.repository;

import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.framework.oss.arguments.bucket.CreateBucketArguments;
import com.ysmjjsy.goya.component.framework.oss.arguments.bucket.DeleteBucketArguments;
import com.ysmjjsy.goya.component.framework.oss.core.repository.OssBucketRepository;
import com.ysmjjsy.goya.component.framework.oss.domain.bucket.BucketDomain;
import com.ysmjjsy.goya.component.oss.minio.converter.arguments.ArgumentsToMakeBucketArgsConverter;
import com.ysmjjsy.goya.component.oss.minio.converter.arguments.ArgumentsToRemoveBucketArgsConverter;
import com.ysmjjsy.goya.component.oss.minio.converter.domain.BucketToDomainConverter;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioService;
import com.ysmjjsy.goya.component.oss.minio.service.MinioBucketService;
import com.ysmjjsy.goya.component.oss.minio.utils.MinioConverterUtils;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Minio Java OSS API 存储桶操作实现</p>
 *
 * @author goya
 * @since 2025/11/1 16:33
 */
@Slf4j
@Service
public class MinioBucketRepository extends BaseMinioService implements OssBucketRepository {

    private final MinioBucketService minioBucketService;

    public MinioBucketRepository(AbstractObjectPool<MinioClient> ossClientObjectPool, MinioBucketService minioBucketService) {
        super(ossClientObjectPool);
        this.minioBucketService = minioBucketService;
    }

    @Override
    public boolean doesBucketExist(String bucketName) {
        return minioBucketService.bucketExists(bucketName);
    }

    @Override
    public List<BucketDomain> listBuckets() {
        return MinioConverterUtils.toDomains(minioBucketService.listBuckets(), new BucketToDomainConverter());
    }

    @Override
    public BucketDomain createBucket(String bucketName) {
        minioBucketService.makeBucket(bucketName);
        return null;
    }

    @Override
    public BucketDomain createBucket(CreateBucketArguments arguments) {
        Converter<CreateBucketArguments, MakeBucketArgs> toArgs = new ArgumentsToMakeBucketArgsConverter();
        minioBucketService.makeBucket(toArgs.convert(arguments));
        return null;
    }

    @Override
    public void deleteBucket(String bucketName) {
        minioBucketService.removeBucket(bucketName);
    }

    @Override
    public void deleteBucket(DeleteBucketArguments arguments) {
        Converter<DeleteBucketArguments, RemoveBucketArgs> toArgs = new ArgumentsToRemoveBucketArgsConverter();
        minioBucketService.removeBucket(toArgs.convert(arguments));
    }


}