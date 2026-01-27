package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 对象标记 Service </p>
 *
 * @author goya
 * @since 2023/7/16 18:47
 */
@Slf4j
@Service
public class S3ObjectTaggingService extends BaseS3Service {

    public S3ObjectTaggingService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除对象标记
     *
     * @param request 删除对象标记请求
     * @return 删除对象标记响应
     * @throws S3Exception S3操作异常
     */
    public DeleteObjectTaggingResponse deleteObjectTagging(DeleteObjectTaggingRequest request) {
        String function = "deleteObjectTagging";
        S3Client client = getClient();
        try {
            return client.deleteObjectTagging(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取对象标记设置
     *
     * @param request 获取对象标记请求
     * @return 获取对象标记响应
     * @throws S3Exception S3操作异常
     */
    public GetObjectTaggingResponse getObjectTagging(GetObjectTaggingRequest request) {
        String function = "getObjectTagging";
        S3Client client = getClient();
        try {
            return client.getObjectTagging(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置对象标记设置
     *
     * @param request 设置对象标记请求
     * @return 设置对象标记响应
     * @throws S3Exception S3操作异常
     */
    public PutObjectTaggingResponse putObjectTagging(PutObjectTaggingRequest request) {
        String function = "putObjectTagging";
        S3Client client = getClient();
        try {
            return client.putObjectTagging(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
