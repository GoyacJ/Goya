package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketAnalyticsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketAnalyticsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketAnalyticsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketAnalyticsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.ListBucketAnalyticsConfigurationsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketAnalyticsConfigurationsResponse;
import software.amazon.awssdk.services.s3.model.PutBucketAnalyticsConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketAnalyticsConfigurationResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 分析配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 16:57
 */
@Slf4j
@Service
public class S3BucketAnalyticsConfigurationService extends BaseS3Service {

    public S3BucketAnalyticsConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶分析配置
     *
     * @param request 删除存储桶分析配置请求
     * @return 删除存储桶分析配置响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketAnalyticsConfigurationResponse deleteBucketAnalyticsConfiguration(DeleteBucketAnalyticsConfigurationRequest request) {
        String function = "deleteBucketAnalyticsConfiguration";
        S3Client client = getClient();
        try {
            DeleteBucketAnalyticsConfigurationResponse response = client.deleteBucketAnalyticsConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶访问分析配置
     *
     * @param request 获取存储桶分析配置请求
     * @return 获取存储桶分析配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketAnalyticsConfigurationResponse getBucketAnalyticsConfiguration(GetBucketAnalyticsConfigurationRequest request) {
        String function = "getBucketAnalyticsConfiguration";
        S3Client client = getClient();
        try {
            GetBucketAnalyticsConfigurationResponse response = client.getBucketAnalyticsConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶访问分析配置列表
     *
     * @param request 列出存储桶分析配置请求
     * @return 列出存储桶分析配置响应
     * @throws S3Exception S3操作异常
     */
    public ListBucketAnalyticsConfigurationsResponse listBucketAnalyticsConfigurations(ListBucketAnalyticsConfigurationsRequest request) {
        String function = "listBucketAnalyticsConfigurations";
        S3Client client = getClient();
        try {
            ListBucketAnalyticsConfigurationsResponse response = client.listBucketAnalyticsConfigurations(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶访问分析配置列表
     *
     * @param request 设置存储桶分析配置请求
     * @return 设置存储桶分析配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketAnalyticsConfigurationResponse putBucketAnalyticsConfiguration(PutBucketAnalyticsConfigurationRequest request) {
        String function = "putBucketAnalyticsConfiguration";
        S3Client client = getClient();
        try {
            PutBucketAnalyticsConfigurationResponse response = client.putBucketAnalyticsConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
