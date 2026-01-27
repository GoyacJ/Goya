package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectLockConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetObjectLockConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutObjectLockConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutObjectLockConfigurationResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * <p>Amazon S3 对象锁定配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 21:14
 */
@Slf4j
@Service
public class S3ObjectLockConfigurationService extends BaseS3Service {

    public S3ObjectLockConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取对象锁定配置
     *
     * @param request 获取对象锁定配置请求
     * @return 获取对象锁定配置响应
     * @throws S3Exception S3操作异常
     */
    public GetObjectLockConfigurationResponse getObjectLockConfiguration(GetObjectLockConfigurationRequest request) {
        String function = "getObjectLockConfiguration";
        S3Client client = getClient();
        try {
            return client.getObjectLockConfiguration(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    /**
     * 设置对象锁定配置
     *
     * @param request 设置对象锁定配置请求
     * @return 设置对象锁定配置响应
     * @throws S3Exception S3操作异常
     */
    public PutObjectLockConfigurationResponse putObjectLockConfiguration(PutObjectLockConfigurationRequest request) {
        String function = "putObjectLockConfiguration";
        S3Client client = getClient();
        try {
            return client.putObjectLockConfiguration(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
