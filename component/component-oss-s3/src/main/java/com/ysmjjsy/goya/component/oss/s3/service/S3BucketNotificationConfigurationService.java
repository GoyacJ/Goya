package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketNotificationConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketNotificationConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketNotificationConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketNotificationConfigurationResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶通知配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 19:13
 */
@Slf4j
@Service
public class S3BucketNotificationConfigurationService extends BaseS3Service {

    public S3BucketNotificationConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取存储桶通知配置
     *
     * @param request 获取存储桶通知配置请求
     * @return 获取存储桶通知配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketNotificationConfigurationResponse getBucketNotificationConfiguration(GetBucketNotificationConfigurationRequest request) {
        String function = "getBucketNotificationConfiguration";
        S3Client client = getClient();
        try {
            GetBucketNotificationConfigurationResponse response = client.getBucketNotificationConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶通知配置
     *
     * @param request 设置存储桶通知配置请求
     * @return 设置存储桶通知配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketNotificationConfigurationResponse putBucketNotificationConfiguration(PutBucketNotificationConfigurationRequest request) {
        String function = "putBucketNotificationConfiguration";
        S3Client client = getClient();
        try {
            PutBucketNotificationConfigurationResponse response = client.putBucketNotificationConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
