package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketWebsiteRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketWebsiteResponse;
import software.amazon.awssdk.services.s3.model.GetBucketWebsiteRequest;
import software.amazon.awssdk.services.s3.model.GetBucketWebsiteResponse;
import software.amazon.awssdk.services.s3.model.PutBucketWebsiteRequest;
import software.amazon.awssdk.services.s3.model.PutBucketWebsiteResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶网页配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:31
 */
@Slf4j
@Service
public class S3BucketWebsiteConfigurationService extends BaseS3Service {

    public S3BucketWebsiteConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶网页配置
     *
     * @param request 删除存储桶网站配置请求
     * @return 删除存储桶网站配置响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketWebsiteResponse deleteBucketWebsite(DeleteBucketWebsiteRequest request) {
        String function = "deleteBucketWebsite";
        S3Client client = getClient();
        try {
            DeleteBucketWebsiteResponse response = client.deleteBucketWebsite(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶网页配置
     *
     * @param request 获取存储桶网站配置请求
     * @return 获取存储桶网站配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketWebsiteResponse getBucketWebsite(GetBucketWebsiteRequest request) {
        String function = "getBucketWebsite";
        S3Client client = getClient();
        try {
            GetBucketWebsiteResponse response = client.getBucketWebsite(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶网页配置
     *
     * @param request 设置存储桶网站配置请求
     * @return 设置存储桶网站配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketWebsiteResponse putBucketWebsite(PutBucketWebsiteRequest request) {
        String function = "putBucketWebsite";
        S3Client client = getClient();
        try {
            PutBucketWebsiteResponse response = client.putBucketWebsite(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
