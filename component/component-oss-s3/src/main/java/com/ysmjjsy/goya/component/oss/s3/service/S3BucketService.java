package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶管理 Service </p>
 *
 * @author goya
 * @since 2023/7/14 16:04
 */
@Slf4j
@Service
public class S3BucketService extends BaseS3Service {

    public S3BucketService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 返回指定存储桶中版本的摘要信息列表
     *
     * @param request 列出对象版本请求
     * @return 列出对象版本响应，包含版本摘要信息列表
     * @throws S3Exception S3操作异常
     */
    public ListObjectVersionsResponse listObjectVersions(ListObjectVersionsRequest request) {
        String function = "listObjectVersions";
        S3Client client = getClient();
        try {
            return client.listObjectVersions(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 此操作可用于确定存储桶是否存在以及您是否有权访问它。如果存储桶存在并且您有权访问，则此操作返回200 OK。
     *
     * @param request HeadBucket请求
     * @return HeadBucket响应
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

    /**
     * 获取存储桶位置
     *
     * @param request 获取存储桶位置请求
     * @return 获取存储桶位置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketLocationResponse getBucketLocation(GetBucketLocationRequest request) {
        String function = "getBucketLocation";
        S3Client client = getClient();
        try {
            return client.getBucketLocation(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
