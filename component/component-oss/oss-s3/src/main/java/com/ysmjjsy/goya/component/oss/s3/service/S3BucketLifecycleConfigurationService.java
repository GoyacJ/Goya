package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketLifecycleRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketLifecycleResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLifecycleConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶生命周期配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:13
 */
@Slf4j
@Service
public class S3BucketLifecycleConfigurationService extends BaseS3Service {

    public S3BucketLifecycleConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶生命周期配置
     *
     * @param request 删除存储桶生命周期配置请求
     * @return 删除存储桶生命周期配置响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketLifecycleResponse deleteBucketLifecycle(DeleteBucketLifecycleRequest request) {
        String function = "deleteBucketLifecycle";
        S3Client client = getClient();
        try {
            DeleteBucketLifecycleResponse response = client.deleteBucketLifecycle(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶生命周期配置
     *
     * @param request 获取存储桶生命周期配置请求
     * @return 获取存储桶生命周期配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketLifecycleConfigurationResponse getBucketLifecycleConfiguration(GetBucketLifecycleConfigurationRequest request) {
        String function = "getBucketLifecycleConfiguration";
        S3Client client = getClient();
        try {
            GetBucketLifecycleConfigurationResponse response = client.getBucketLifecycleConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶生命周期配置
     *
     * @param request 设置存储桶生命周期配置请求
     * @return 设置存储桶生命周期配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketLifecycleConfigurationResponse putBucketLifecycleConfiguration(PutBucketLifecycleConfigurationRequest request) {
        String function = "putBucketLifecycleConfiguration";
        S3Client client = getClient();
        try {
            PutBucketLifecycleConfigurationResponse response = client.putBucketLifecycleConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
