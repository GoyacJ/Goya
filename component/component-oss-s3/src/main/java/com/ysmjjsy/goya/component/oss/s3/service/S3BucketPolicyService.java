package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketPolicyResponse;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyResponse;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyStatusRequest;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyStatusResponse;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶访问策略 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:23
 */
@Slf4j
@Service
public class S3BucketPolicyService extends BaseS3Service {

    public S3BucketPolicyService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶访问策略
     *
     * @param request 删除存储桶策略请求
     * @return 删除存储桶策略响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketPolicyResponse deleteBucketPolicy(DeleteBucketPolicyRequest request) {
        String function = "deleteBucketPolicy";
        S3Client client = getClient();
        try {
            return client.deleteBucketPolicy(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储访问策略
     *
     * @param request 获取存储桶策略请求
     * @return 获取存储桶策略响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketPolicyResponse getBucketPolicy(GetBucketPolicyRequest request) {
        String function = "getBucketPolicy";
        S3Client client = getClient();
        try {
            return client.getBucketPolicy(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储访问策略状态
     *
     * @param request 获取存储桶策略状态请求
     * @return 获取存储桶策略状态响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketPolicyStatusResponse getBucketPolicyStatus(GetBucketPolicyStatusRequest request) {
        String function = "getBucketPolicyStatus";
        S3Client client = getClient();
        try {
            return client.getBucketPolicyStatus(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储访问策略
     *
     * @param request 设置存储桶策略请求
     * @return 设置存储桶策略响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketPolicyResponse putBucketPolicy(PutBucketPolicyRequest request) {
        String function = "putBucketPolicy";
        S3Client client = getClient();
        try {
            return client.putBucketPolicy(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
