package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectLegalHoldRequest;
import software.amazon.awssdk.services.s3.model.GetObjectLegalHoldResponse;
import software.amazon.awssdk.services.s3.model.PutObjectLegalHoldRequest;
import software.amazon.awssdk.services.s3.model.PutObjectLegalHoldResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 对象访合法持有 Service </p>
 *
 * @author goya
 * @since 2023/7/16 21:14
 */
@Slf4j
@Service
public class S3ObjectLegalHoldService extends BaseS3Service {

    public S3ObjectLegalHoldService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取对象合法持有设置
     *
     * @param request 获取对象合法持有请求
     * @return 获取对象合法持有响应
     * @throws S3Exception S3操作异常
     */
    public GetObjectLegalHoldResponse getObjectLegalHold(GetObjectLegalHoldRequest request) {
        String function = "getObjectLegalHold";
        S3Client client = getClient();
        try {
            GetObjectLegalHoldResponse response = client.getObjectLegalHold(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置对象合法持有设置
     *
     * @param request 设置对象合法持有请求
     * @return 设置对象合法持有响应
     * @throws S3Exception S3操作异常
     */
    public PutObjectLegalHoldResponse putObjectLegalHold(PutObjectLegalHoldRequest request) {
        String function = "putObjectLegalHold";
        S3Client client = getClient();
        try {
            PutObjectLegalHoldResponse response = client.putObjectLegalHold(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
