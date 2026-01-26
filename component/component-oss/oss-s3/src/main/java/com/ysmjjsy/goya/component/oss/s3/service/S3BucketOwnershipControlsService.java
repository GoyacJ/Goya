package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketOwnershipControlsRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketOwnershipControlsResponse;
import software.amazon.awssdk.services.s3.model.GetBucketOwnershipControlsRequest;
import software.amazon.awssdk.services.s3.model.GetBucketOwnershipControlsResponse;
import software.amazon.awssdk.services.s3.model.PutBucketOwnershipControlsRequest;
import software.amazon.awssdk.services.s3.model.PutBucketOwnershipControlsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶所有权控制 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:20
 */
@Slf4j
@Service
public class S3BucketOwnershipControlsService extends BaseS3Service {

    public S3BucketOwnershipControlsService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶所有权控制
     *
     * @param request 删除存储桶所有权控制请求
     * @return 删除存储桶所有权控制响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketOwnershipControlsResponse deleteBucketOwnershipControls(DeleteBucketOwnershipControlsRequest request) {
        String function = "deleteBucketOwnershipControls";
        S3Client client = getClient();
        try {
            DeleteBucketOwnershipControlsResponse response = client.deleteBucketOwnershipControls(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶所有权控制
     *
     * @param request 获取存储桶所有权控制请求
     * @return 获取存储桶所有权控制响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketOwnershipControlsResponse getBucketOwnershipControls(GetBucketOwnershipControlsRequest request) {
        String function = "getBucketOwnershipControls";
        S3Client client = getClient();
        try {
            GetBucketOwnershipControlsResponse response = client.getBucketOwnershipControls(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶所有权控制
     *
     * @param request 设置存储桶所有权控制请求
     * @return 设置存储桶所有权控制响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketOwnershipControlsResponse putBucketOwnershipControls(PutBucketOwnershipControlsRequest request) {
        String function = "putBucketOwnershipControls";
        S3Client client = getClient();
        try {
            PutBucketOwnershipControlsResponse response = client.putBucketOwnershipControls(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
