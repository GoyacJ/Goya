package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketLoggingRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLoggingResponse;
import software.amazon.awssdk.services.s3.model.PutBucketLoggingRequest;
import software.amazon.awssdk.services.s3.model.PutBucketLoggingResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶日志配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 19:09
 */
@Slf4j
@Service
public class S3BucketLoggingConfigurationService extends BaseS3Service {

    public S3BucketLoggingConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取存储桶日志配置
     *
     * @param request 获取存储桶日志配置请求
     * @return 获取存储桶日志配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketLoggingResponse getBucketLogging(GetBucketLoggingRequest request) {
        String function = "getBucketLogging";
        S3Client client = getClient();
        try {
            GetBucketLoggingResponse response = client.getBucketLogging(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶日志配置
     *
     * @param request 设置存储桶日志配置请求
     * @return 设置存储桶日志配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketLoggingResponse putBucketLogging(PutBucketLoggingRequest request) {
        String function = "putBucketLogging";
        S3Client client = getClient();
        try {
            PutBucketLoggingResponse response = client.putBucketLogging(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
