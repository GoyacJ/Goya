package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * <p>获取请求的已验证发件人正在使用的Amazon Web Services帐户的当前所有者。 </p>
 *
 * @author goya
 * @since 2023/7/16 22:16
 */
@Slf4j
@Service
public class S3AccountOwnerService extends BaseS3Service {

    public S3AccountOwnerService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取存储桶所有者
     *
     * @param request 获取存储桶所有权控制请求
     * @return 存储桶所有权控制响应，包含所有者信息
     * @throws S3Exception S3操作异常
     */
    public GetBucketOwnershipControlsResponse getBucketOwner(GetBucketOwnershipControlsRequest request) {
        String function = "getBucketOwner";
        S3Client client = getClient();
        try {
            return client.getBucketOwnershipControls(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶所有者（通过 HeadBucket）
     *
     * @param request HeadBucket请求
     * @return HeadBucket响应，包含所有者信息
     * @throws S3Exception S3操作异常
     */
    public HeadBucketResponse headBucket(HeadBucketRequest request) {
        String function = "headBucket";
        S3Client client = getClient();
        try {
            return client.headBucket(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
