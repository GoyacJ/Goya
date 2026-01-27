package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketAccelerateConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketAccelerateConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketAccelerateConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketAccelerateConfigurationResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶加速配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 18:52
 */
@Slf4j
@Service
public class S3BucketAccelerateConfigurationService extends BaseS3Service {

    public S3BucketAccelerateConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取存储桶加速配置
     *
     * @param request 获取存储桶加速配置请求
     * @return 获取存储桶加速配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketAccelerateConfigurationResponse getBucketAccelerateConfiguration(GetBucketAccelerateConfigurationRequest request) {
        String function = "getBucketAccelerateConfiguration";
        S3Client client = getClient();
        try {
            return client.getBucketAccelerateConfiguration(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶加速配置
     *
     * @param request 设置存储桶加速配置请求
     * @return 设置存储桶加速配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketAccelerateConfigurationResponse putBucketAccelerateConfiguration(PutBucketAccelerateConfigurationRequest request) {
        String function = "putBucketAccelerateConfiguration";
        S3Client client = getClient();
        try {
            return client.putBucketAccelerateConfiguration(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
