package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningResponse;
import software.amazon.awssdk.services.s3.model.PutBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.PutBucketVersioningResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶版本配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 19:22
 */
@Slf4j
@Service
public class S3BucketVersioningConfigurationService extends BaseS3Service {

    public S3BucketVersioningConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取存储桶版本配置
     *
     * @param request 获取存储桶版本配置请求
     * @return 获取存储桶版本配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketVersioningResponse getBucketVersioning(GetBucketVersioningRequest request) {
        String function = "getBucketVersioning";
        S3Client client = getClient();
        try {
            GetBucketVersioningResponse response = client.getBucketVersioning(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶版本配置
     *
     * @param request 设置存储桶版本配置请求
     * @return 设置存储桶版本配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketVersioningResponse putBucketVersioning(PutBucketVersioningRequest request) {
        String function = "putBucketVersioning";
        S3Client client = getClient();
        try {
            PutBucketVersioningResponse response = client.putBucketVersioning(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
