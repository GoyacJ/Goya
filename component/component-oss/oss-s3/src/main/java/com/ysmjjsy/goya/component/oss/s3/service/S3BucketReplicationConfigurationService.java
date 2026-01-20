package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketReplicationRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketReplicationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketReplicationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketReplicationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketReplicationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketReplicationResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶复制配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:25
 */
@Slf4j
@Service
public class S3BucketReplicationConfigurationService extends BaseS3Service {

    public S3BucketReplicationConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶复制配置
     *
     * @param request 删除存储桶复制配置请求
     * @return 删除存储桶复制配置响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketReplicationResponse deleteBucketReplication(DeleteBucketReplicationRequest request) {
        String function = "deleteBucketReplication";
        S3Client client = getClient();
        try {
            DeleteBucketReplicationResponse response = client.deleteBucketReplication(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储复制配置
     *
     * @param request 获取存储桶复制配置请求
     * @return 获取存储桶复制配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketReplicationResponse getBucketReplication(GetBucketReplicationRequest request) {
        String function = "getBucketReplication";
        S3Client client = getClient();
        try {
            GetBucketReplicationResponse response = client.getBucketReplication(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储复制配置
     *
     * @param request 设置存储桶复制配置请求
     * @return 设置存储桶复制配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketReplicationResponse putBucketReplication(PutBucketReplicationRequest request) {
        String function = "putBucketReplication";
        S3Client client = getClient();
        try {
            PutBucketReplicationResponse response = client.putBucketReplication(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
