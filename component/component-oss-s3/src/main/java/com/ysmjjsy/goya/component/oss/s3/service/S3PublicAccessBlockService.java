package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeletePublicAccessBlockRequest;
import software.amazon.awssdk.services.s3.model.DeletePublicAccessBlockResponse;
import software.amazon.awssdk.services.s3.model.GetPublicAccessBlockRequest;
import software.amazon.awssdk.services.s3.model.GetPublicAccessBlockResponse;
import software.amazon.awssdk.services.s3.model.PutPublicAccessBlockRequest;
import software.amazon.awssdk.services.s3.model.PutPublicAccessBlockResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 公共访问块 Service </p>
 *
 * @author goya
 * @since 2023/7/16 18:49
 */
@Slf4j
@Service
public class S3PublicAccessBlockService extends BaseS3Service {

    public S3PublicAccessBlockService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除公共访问块
     *
     * @param request 删除公共访问块请求
     * @return 删除公共访问块响应
     * @throws S3Exception S3操作异常
     */
    public DeletePublicAccessBlockResponse deletePublicAccessBlock(DeletePublicAccessBlockRequest request) {
        String function = "deletePublicAccessBlock";
        S3Client client = getClient();
        try {
            return client.deletePublicAccessBlock(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取公共访问块
     *
     * @param request 获取公共访问块请求
     * @return 获取公共访问块响应
     * @throws S3Exception S3操作异常
     */
    public GetPublicAccessBlockResponse getPublicAccessBlock(GetPublicAccessBlockRequest request) {
        String function = "getPublicAccessBlock";
        S3Client client = getClient();
        try {
            return client.getPublicAccessBlock(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置公共访问块
     *
     * @param request 设置公共访问块请求
     * @return 设置公共访问块响应
     * @throws S3Exception S3操作异常
     */
    public PutPublicAccessBlockResponse putPublicAccessBlock(PutPublicAccessBlockRequest request) {
        String function = "putPublicAccessBlock";
        S3Client client = getClient();
        try {
            return client.putPublicAccessBlock(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
