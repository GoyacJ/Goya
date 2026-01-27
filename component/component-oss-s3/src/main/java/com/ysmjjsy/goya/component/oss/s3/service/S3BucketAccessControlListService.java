package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketAclRequest;
import software.amazon.awssdk.services.s3.model.GetBucketAclResponse;
import software.amazon.awssdk.services.s3.model.PutBucketAclRequest;
import software.amazon.awssdk.services.s3.model.PutBucketAclResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶访问控制列表 Service </p>
 *
 * @author goya
 * @since 2023/7/16 18:55
 */
@Slf4j
@Service
public class S3BucketAccessControlListService extends BaseS3Service {

    public S3BucketAccessControlListService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取存储桶访问控制列表
     *
     * @param request 获取存储桶ACL请求
     * @return 获取存储桶ACL响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketAclResponse getBucketAcl(GetBucketAclRequest request) {
        String function = "getBucketAcl";
        S3Client client = getClient();
        try {
            return client.getBucketAcl(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶访问控制列表
     *
     * @param request 设置存储桶ACL请求
     * @return 设置存储桶ACL响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketAclResponse putBucketAcl(PutBucketAclRequest request) {
        String function = "putBucketAcl";
        S3Client client = getClient();
        try {
            return client.putBucketAcl(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
