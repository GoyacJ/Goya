package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketInventoryConfigurationRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketInventoryConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketInventoryConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketInventoryConfigurationResponse;
import software.amazon.awssdk.services.s3.model.ListBucketInventoryConfigurationsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketInventoryConfigurationsResponse;
import software.amazon.awssdk.services.s3.model.PutBucketInventoryConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketInventoryConfigurationResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 存储桶库存配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:11
 */
@Slf4j
@Service
public class S3BucketInventoryConfiguration extends BaseS3Service {

    public S3BucketInventoryConfiguration(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶库存配置
     *
     * @param request 删除存储桶库存配置请求
     * @return 删除存储桶库存配置响应
     * @throws S3Exception S3操作异常
     */
    public DeleteBucketInventoryConfigurationResponse deleteBucketInventoryConfiguration(DeleteBucketInventoryConfigurationRequest request) {
        String function = "deleteBucketInventoryConfiguration";
        S3Client client = getClient();
        try {
            DeleteBucketInventoryConfigurationResponse response = client.deleteBucketInventoryConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶库存配置
     *
     * @param request 获取存储桶库存配置请求
     * @return 获取存储桶库存配置响应
     * @throws S3Exception S3操作异常
     */
    public GetBucketInventoryConfigurationResponse getBucketInventoryConfiguration(GetBucketInventoryConfigurationRequest request) {
        String function = "getBucketInventoryConfiguration";
        S3Client client = getClient();
        try {
            GetBucketInventoryConfigurationResponse response = client.getBucketInventoryConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶库存配置列表
     *
     * @param request 列出存储桶库存配置请求
     * @return 列出存储桶库存配置响应
     * @throws S3Exception S3操作异常
     */
    public ListBucketInventoryConfigurationsResponse listBucketInventoryConfigurations(ListBucketInventoryConfigurationsRequest request) {
        String function = "listBucketInventoryConfigurations";
        S3Client client = getClient();
        try {
            ListBucketInventoryConfigurationsResponse response = client.listBucketInventoryConfigurations(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置存储桶库存配置列表
     *
     * @param request 设置存储桶库存配置请求
     * @return 设置存储桶库存配置响应
     * @throws S3Exception S3操作异常
     */
    public PutBucketInventoryConfigurationResponse putBucketInventoryConfiguration(PutBucketInventoryConfigurationRequest request) {
        String function = "putBucketInventoryConfiguration";
        S3Client client = getClient();
        try {
            PutBucketInventoryConfigurationResponse response = client.putBucketInventoryConfiguration(request);
            return response;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
