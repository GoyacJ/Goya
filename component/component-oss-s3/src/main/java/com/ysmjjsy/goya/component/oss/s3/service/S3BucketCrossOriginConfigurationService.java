package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketCorsRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketCorsResponse;
import software.amazon.awssdk.services.s3.model.GetBucketCorsRequest;
import software.amazon.awssdk.services.s3.model.GetBucketCorsResponse;
import software.amazon.awssdk.services.s3.model.PutBucketCorsRequest;
import software.amazon.awssdk.services.s3.model.PutBucketCorsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 跨域配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:01
 */
@Slf4j
@Service
public class S3BucketCrossOriginConfigurationService extends BaseS3Service {

    public S3BucketCrossOriginConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶跨域配置
     *
     * @param request 删除存储桶CORS配置请求
     * @return 删除存储桶CORS配置响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketCorsResponse deleteBucketCors(DeleteBucketCorsRequest request) {
        String function = "deleteBucketCors";
        S3Client client = getClient();
        try {
            DeleteBucketCorsResponse response = client.deleteBucketCors(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶跨域配置
     *
     * @param request 获取存储桶CORS配置请求
     * @return 获取存储桶CORS配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketCorsResponse getBucketCors(GetBucketCorsRequest request) {
        String function = "getBucketCors";
        S3Client client = getClient();
        try {
            GetBucketCorsResponse response = client.getBucketCors(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶跨域配置
     *
     * @param request 设置存储桶CORS配置请求
     * @return 设置存储桶CORS配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketCorsResponse putBucketCors(PutBucketCorsRequest request) {
        String function = "putBucketCors";
        S3Client client = getClient();
        try {
            PutBucketCorsResponse response = client.putBucketCors(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
