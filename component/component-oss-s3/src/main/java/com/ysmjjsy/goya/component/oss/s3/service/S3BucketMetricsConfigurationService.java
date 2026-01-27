package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketMetricsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketMetricsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketMetricsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketMetricsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.ListBucketMetricsConfigurationsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketMetricsConfigurationsResponse;
import software.amazon.awssdk.services.s3.model.PutBucketMetricsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketMetricsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶生命度量配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:16
 */
@Slf4j
@Service
public class S3BucketMetricsConfigurationService extends BaseS3Service {

    public S3BucketMetricsConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶度量配置
     *
     * @param request 删除存储桶度量配置请求
     * @return 删除存储桶度量配置响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketMetricsConfigurationResponse deleteBucketMetricsConfiguration(DeleteBucketMetricsConfigurationRequest request) {
        String function = "deleteBucketMetricsConfiguration";
        S3Client client = getClient();
        try {
            return client.deleteBucketMetricsConfiguration(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶度量配置
     *
     * @param request 获取存储桶度量配置请求
     * @return 获取存储桶度量配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketMetricsConfigurationResponse getBucketMetricsConfiguration(GetBucketMetricsConfigurationRequest request) {
        String function = "getBucketMetricsConfiguration";
        S3Client client = getClient();
        try {
            return client.getBucketMetricsConfiguration(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶度量配置列表
     *
     * @param request 列出存储桶度量配置请求
     * @return 列出存储桶度量配置响应
     * @throws S3Exception S3操作异常
     */
    public ListBucketMetricsConfigurationsResponse listBucketMetricsConfigurations(ListBucketMetricsConfigurationsRequest request) {
        String function = "listBucketMetricsConfigurations";
        S3Client client = getClient();
        try {
            return client.listBucketMetricsConfigurations(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶度量配置列表
     *
     * @param request 设置存储桶度量配置请求
     * @return 设置存储桶度量配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketMetricsConfigurationResponse putBucketMetricsConfiguration(PutBucketMetricsConfigurationRequest request) {
        String function = "putBucketMetricsConfiguration";
        S3Client client = getClient();
        try {
            return client.putBucketMetricsConfiguration(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
