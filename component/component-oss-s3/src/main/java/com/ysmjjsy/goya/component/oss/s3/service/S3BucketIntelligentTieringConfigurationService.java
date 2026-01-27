package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketIntelligentTieringConfigurationRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketIntelligentTieringConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketIntelligentTieringConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketIntelligentTieringConfigurationResponse;
import software.amazon.awssdk.services.s3.model.ListBucketIntelligentTieringConfigurationsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketIntelligentTieringConfigurationsResponse;
import software.amazon.awssdk.services.s3.model.PutBucketIntelligentTieringConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketIntelligentTieringConfigurationResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶智能分层配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:08
 */
@Slf4j
@Service
public class S3BucketIntelligentTieringConfigurationService extends BaseS3Service {

    public S3BucketIntelligentTieringConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶智能分层配置
     *
     * @param request 删除存储桶智能分层配置请求
     * @return 删除存储桶智能分层配置响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketIntelligentTieringConfigurationResponse deleteBucketIntelligentTieringConfiguration(DeleteBucketIntelligentTieringConfigurationRequest request) {
        String function = "deleteBucketIntelligentTieringConfiguration";
        S3Client client = getClient();
        try {
            DeleteBucketIntelligentTieringConfigurationResponse response = client.deleteBucketIntelligentTieringConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶智能分层配置
     *
     * @param request 获取存储桶智能分层配置请求
     * @return 获取存储桶智能分层配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketIntelligentTieringConfigurationResponse getBucketIntelligentTieringConfiguration(GetBucketIntelligentTieringConfigurationRequest request) {
        String function = "getBucketIntelligentTieringConfiguration";
        S3Client client = getClient();
        try {
            GetBucketIntelligentTieringConfigurationResponse response = client.getBucketIntelligentTieringConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶智能分层配置列表
     *
     * @param request 列出存储桶智能分层配置请求
     * @return 列出存储桶智能分层配置响应
     * @throws S3Exception S3操作异常
     */
    public ListBucketIntelligentTieringConfigurationsResponse listBucketIntelligentTieringConfigurations(ListBucketIntelligentTieringConfigurationsRequest request) {
        String function = "listBucketIntelligentTieringConfigurations";
        S3Client client = getClient();
        try {
            ListBucketIntelligentTieringConfigurationsResponse response = client.listBucketIntelligentTieringConfigurations(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶智能分层配置列表
     *
     * @param request 设置存储桶智能分层配置请求
     * @return 设置存储桶智能分层配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketIntelligentTieringConfigurationResponse putBucketIntelligentTieringConfiguration(PutBucketIntelligentTieringConfigurationRequest request) {
        String function = "putBucketIntelligentTieringConfiguration";
        S3Client client = getClient();
        try {
            PutBucketIntelligentTieringConfigurationResponse response = client.putBucketIntelligentTieringConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
