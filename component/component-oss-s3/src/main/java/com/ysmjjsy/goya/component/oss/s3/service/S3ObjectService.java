package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * <p>Amazon S3 对象管理 Service </p>
 *
 * @author goya
 * @since 2023/7/16 16:48
 */
@Slf4j
@Service
public class S3ObjectService extends BaseS3Service {

    public S3ObjectService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取对象详细信息
     *
     * @param request HeadObject请求
     * @return HeadObject响应，包含对象元数据信息
     * @throws S3Exception S3操作异常
     */
    public HeadObjectResponse headObject(HeadObjectRequest request) {
        String function = "headObject";
        S3Client client = getClient();
        try {
            return client.headObject(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 上传对象
     *
     * @param request PutObject请求
     * @param requestBody 请求体，包含对象内容
     * @return PutObject响应
     * @throws S3Exception S3操作异常
     */
    public PutObjectResponse putObject(PutObjectRequest request, RequestBody requestBody) {
        String function = "putObject";
        S3Client client = getClient();
        try {
            return client.putObject(request, requestBody);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 复制对象
     *
     * @param request CopyObject请求
     * @return CopyObject响应
     * @throws S3Exception S3操作异常
     */
    public CopyObjectResponse copyObject(CopyObjectRequest request) {
        String function = "copyObject";
        S3Client client = getClient();
        try {
            return client.copyObject(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 删除对象
     *
     * @param request DeleteObject请求
     * @return DeleteObject响应
     * @throws S3Exception S3操作异常
     */
    public DeleteObjectResponse deleteObject(DeleteObjectRequest request) {
        String function = "deleteObject";
        S3Client client = getClient();
        try {
            return client.deleteObject(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 列出下一批对象
     *
     * @param request ListObjectsV2请求
     * @return ListObjectsV2响应，包含对象列表
     * @throws S3Exception S3操作异常
     */
    public ListObjectsV2Response listObjectsV2(ListObjectsV2Request request) {
        String function = "listObjectsV2";
        S3Client client = getClient();
        try {
            return client.listObjectsV2(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
