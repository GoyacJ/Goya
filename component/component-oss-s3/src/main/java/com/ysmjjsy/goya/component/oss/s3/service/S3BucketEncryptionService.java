package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketEncryptionRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketEncryptionResponse;
import software.amazon.awssdk.services.s3.model.GetBucketEncryptionRequest;
import software.amazon.awssdk.services.s3.model.GetBucketEncryptionResponse;
import software.amazon.awssdk.services.s3.model.PutBucketEncryptionRequest;
import software.amazon.awssdk.services.s3.model.PutBucketEncryptionResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶加密 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:04
 */
@Slf4j
@Service
public class S3BucketEncryptionService extends BaseS3Service {

    public S3BucketEncryptionService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶加密
     *
     * @param request 删除存储桶加密配置请求
     * @return 删除存储桶加密配置响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketEncryptionResponse deleteBucketEncryption(DeleteBucketEncryptionRequest request) {
        String function = "deleteBucketEncryption";
        S3Client client = getClient();
        try {
            return client.deleteBucketEncryption(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶加密
     *
     * @param request 获取存储桶加密配置请求
     * @return 获取存储桶加密配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketEncryptionResponse getBucketEncryption(GetBucketEncryptionRequest request) {
        String function = "getBucketEncryption";
        S3Client client = getClient();
        try {
            return client.getBucketEncryption(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶加密
     *
     * @param request 设置存储桶加密配置请求
     * @return 设置存储桶加密配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketEncryptionResponse putBucketEncryption(PutBucketEncryptionRequest request) {
        String function = "putBucketEncryption";
        S3Client client = getClient();
        try {
            return client.putBucketEncryption(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
