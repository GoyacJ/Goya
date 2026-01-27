package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.oss.minio.converter.retention.ObjectLockConfigurationToDomainConverter;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioService;
import com.ysmjjsy.goya.component.oss.minio.domain.ObjectLockConfigurationDomain;
import io.minio.DeleteObjectLockConfigurationArgs;
import io.minio.GetObjectLockConfigurationArgs;
import io.minio.MinioClient;
import io.minio.SetObjectLockConfigurationArgs;
import io.minio.errors.*;
import io.minio.messages.ObjectLockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Minio 对象锁定配置</p>
 *
 * @author goya
 * @since 2025/11/1 16:55
 */
@Slf4j
@Service
public class MinioObjectLockConfigurationService extends BaseMinioService {
    private final Converter<ObjectLockConfiguration, ObjectLockConfigurationDomain> toDo;

    public MinioObjectLockConfigurationService(MinioClientObjectPool minioClientObjectPool) {
        super(minioClientObjectPool);
        this.toDo = new ObjectLockConfigurationToDomainConverter();
    }

    /**
     * 获取对象锁定配置
     *
     * @param bucketName 存储桶名称
     * @return {@link ObjectLockConfiguration}
     */
    public ObjectLockConfiguration getObjectLockConfiguration(String bucketName) {
        return getObjectLockConfiguration(bucketName, null);
    }

    /**
     * 获取对象锁定配置
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @return {@link ObjectLockConfiguration}
     */
    public ObjectLockConfiguration getObjectLockConfiguration(String bucketName, String region) {
        return getObjectLockConfiguration(GetObjectLockConfigurationArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 获取对象锁定配置
     *
     * @param getObjectLockConfigurationArgs {@link GetObjectLockConfigurationArgs}
     * @return {@link ObjectLockConfiguration}
     */
    public ObjectLockConfiguration getObjectLockConfiguration(GetObjectLockConfigurationArgs getObjectLockConfigurationArgs) {
        String function = "getObjectLockConfiguration";
        MinioClient minioClient = getClient();

        try {
            return minioClient.getObjectLockConfiguration(getObjectLockConfigurationArgs);
        } catch (ErrorResponseException e) {
            // 如果没有设置过 ObjectLock getObjectLockConfiguration 方法会抛出 ErrorResponseException
            return null;
        } catch (InsufficientDataException e) {
            log.error("[Goya] |- Minio catch InsufficientDataException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InternalException e) {
            log.error("[Goya] |- Minio catch InternalException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidResponseException e) {
            log.error("[Goya] |- Minio catch InvalidResponseException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (ServerException e) {
            log.error("[Goya] |- Minio catch ServerException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (XmlParserException e) {
            log.error("[Goya] |- Minio catch XmlParserException in createBucket.", e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }

    /**
     * 设置对象锁定
     *
     * @param bucketName 存储桶名称
     * @param config     对象锁定配置 {@link ObjectLockConfiguration}
     */
    public void setObjectLockConfiguration(String bucketName, ObjectLockConfiguration config) {
        setObjectLockConfiguration(bucketName, null, config);
    }

    /**
     * 设置对象锁定
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @param config     对象锁定配置 {@link ObjectLockConfiguration}
     */
    public void setObjectLockConfiguration(String bucketName, String region, ObjectLockConfiguration config) {
        setObjectLockConfiguration(SetObjectLockConfigurationArgs.builder().bucket(bucketName).region(region).config(config).build());
    }

    /**
     * 设置对象锁定
     *
     * @param setObjectLockConfigurationArgs {@link SetObjectLockConfigurationArgs}
     */
    public void setObjectLockConfiguration(SetObjectLockConfigurationArgs setObjectLockConfigurationArgs) {
        String function = "setObjectLockConfiguration";
        MinioClient minioClient = getClient();

        try {
            minioClient.setObjectLockConfiguration(setObjectLockConfigurationArgs);
        } catch (ErrorResponseException e) {
            log.error("[Goya] |- Minio catch ErrorResponseException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InsufficientDataException e) {
            log.error("[Goya] |- Minio catch InsufficientDataException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InternalException e) {
            log.error("[Goya] |- Minio catch InternalException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidResponseException e) {
            log.error("[Goya] |- Minio catch InvalidResponseException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (ServerException e) {
            log.error("[Goya] |- Minio catch ServerException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (XmlParserException e) {
            log.error("[Goya] |- Minio catch XmlParserException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }

    /**
     * 删除对象锁定配置
     *
     * @param bucketName 存储桶名称
     */
    public void deleteObjectLockConfiguration(String bucketName) {
        deleteObjectLockConfiguration(bucketName, null);
    }

    /**
     * 删除对象锁定配置
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     */
    public void deleteObjectLockConfiguration(String bucketName, String region) {
        deleteObjectLockConfiguration(DeleteObjectLockConfigurationArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 删除对象锁定
     *
     * @param deleteObjectLockConfigurationArgs {@link DeleteObjectLockConfigurationArgs}
     */
    public void deleteObjectLockConfiguration(DeleteObjectLockConfigurationArgs deleteObjectLockConfigurationArgs) {
        String function = "deleteObjectLockConfiguration";
        MinioClient minioClient = getClient();

        try {
            minioClient.deleteObjectLockConfiguration(deleteObjectLockConfigurationArgs);
        } catch (ErrorResponseException e) {
            log.error("[Goya] |- Minio catch ErrorResponseException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InsufficientDataException e) {
            log.error("[Goya] |- Minio catch InsufficientDataException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InternalException e) {
            log.error("[Goya] |- Minio catch InternalException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidResponseException e) {
            log.error("[Goya] |- Minio catch InvalidResponseException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (ServerException e) {
            log.error("[Goya] |- Minio catch ServerException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (XmlParserException e) {
            log.error("[Goya] |- Minio catch XmlParserException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }
}
