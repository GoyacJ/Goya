package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRetentionRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRetentionResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRetentionRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRetentionResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 对象保留设置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 21:14
 */
@Slf4j
@Service
public class S3ObjectRetentionService extends BaseS3Service {

    public S3ObjectRetentionService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取对象保留设置
     *
     * @param request 获取对象保留请求
     * @return 获取对象保留响应
     * @throws S3Exception S3操作异常
     */
    public GetObjectRetentionResponse getObjectRetention(GetObjectRetentionRequest request) {
        String function = "getObjectRetention";
        S3Client client = getClient();
        try {
            GetObjectRetentionResponse response = client.getObjectRetention(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置对象保留设置
     *
     * @param request 设置对象保留请求
     * @return 设置对象保留响应
     * @throws S3Exception S3操作异常
     */
    public PutObjectRetentionResponse putObjectRetention(PutObjectRetentionRequest request) {
        String function = "putObjectRetention";
        S3Client client = getClient();
        try {
            PutObjectRetentionResponse response = client.putObjectRetention(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
