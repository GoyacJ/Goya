package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketTaggingRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketTaggingResponse;
import software.amazon.awssdk.services.s3.model.GetBucketTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetBucketTaggingResponse;
import software.amazon.awssdk.services.s3.model.PutBucketTaggingRequest;
import software.amazon.awssdk.services.s3.model.PutBucketTaggingResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶标记配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:28
 */
@Slf4j
@Service
public class S3BucketTaggingConfigurationService extends BaseS3Service {

    public S3BucketTaggingConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶标记配置
     *
     * @param request 删除存储桶标记配置请求
     * @return 删除存储桶标记配置响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketTaggingResponse deleteBucketTagging(DeleteBucketTaggingRequest request) {
        String function = "deleteBucketTagging";
        S3Client client = getClient();
        try {
            DeleteBucketTaggingResponse response = client.deleteBucketTagging(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储标记配置
     *
     * @param request 获取存储桶标记配置请求
     * @return 获取存储桶标记配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketTaggingResponse getBucketTagging(GetBucketTaggingRequest request) {
        String function = "getBucketTagging";
        S3Client client = getClient();
        try {
            GetBucketTaggingResponse response = client.getBucketTagging(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储标记配置
     *
     * @param request 设置存储桶标记配置请求
     * @return 设置存储桶标记配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketTaggingResponse putBucketTagging(PutBucketTaggingRequest request) {
        String function = "putBucketTagging";
        S3Client client = getClient();
        try {
            PutBucketTaggingResponse response = client.putBucketTagging(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
