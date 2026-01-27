package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.oss.minio.converter.retention.RetentionToDomainConverter;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioService;
import com.ysmjjsy.goya.component.oss.minio.domain.RetentionDomain;
import io.minio.GetObjectRetentionArgs;
import io.minio.MinioClient;
import io.minio.SetObjectRetentionArgs;
import io.minio.errors.*;
import io.minio.messages.Retention;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Minio 对象保留配置</p>
 *
 * @author goya
 * @since 2025/11/1 16:56
 */
@Slf4j
@Service
public class MinioObjectRetentionService extends BaseMinioService {

    private final Converter<Retention, RetentionDomain> toDo;

    public MinioObjectRetentionService(MinioClientObjectPool minioClientObjectPool) {
        super(minioClientObjectPool);
        this.toDo = new RetentionToDomainConverter();
    }

    /**
     * 获取对象的保留配置
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 自定义保留域对象
     */
    public RetentionDomain getObjectRetention(String bucketName, String objectName) {
        return getObjectRetention(bucketName, null, objectName);
    }

    /**
     * 获取对象的保留配置
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @param objectName 对象名称
     * @return 自定义保留域对象
     */
    public RetentionDomain getObjectRetention(String bucketName, String region, String objectName) {
        return getObjectRetention(bucketName, region, objectName, null);
    }

    /**
     * 获取对象的保留配置
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @param objectName 对象名称
     * @param versionId  版本ID
     * @return 自定义保留域对象
     */
    public RetentionDomain getObjectRetention(String bucketName, String region, String objectName, String versionId) {
        return getObjectRetention(GetObjectRetentionArgs.builder().bucket(bucketName).region(region).object(objectName).versionId(versionId).build());
    }

    /**
     * 获取对象的保留配置
     *
     * @param getObjectRetentionArgs {@link GetObjectRetentionArgs}
     * @return {@link RetentionDomain}
     */
    public RetentionDomain getObjectRetention(GetObjectRetentionArgs getObjectRetentionArgs) {
        String function = "getObjectRetention";
        MinioClient minioClient = getClient();

        try {
            Retention retention = minioClient.getObjectRetention(getObjectRetentionArgs);
            return toDo.convert(retention);
        } catch (ErrorResponseException e) {
            log.error("[Goya] |- Minio catch ErrorResponseException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InsufficientDataException e) {
            log.error("[Goya] |- Minio catch InsufficientDataException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InternalException e) {
            log.error("[Goya] |- Minio catch InternalException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InvalidResponseException e) {
            log.error("[Goya] |- Minio catch InvalidResponseException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (ServerException e) {
            log.error("[Goya] |- Minio catch ServerException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (XmlParserException e) {
            log.error("[Goya] |- Minio catch XmlParserException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }

    /**
     * 添加对象的保留配置，存储桶需要设置为对象锁定模式，并且没有开启版本控制，否则会报错收蠕虫保护。
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param config     保留配置 {@link Retention}
     */
    public void setObjectRetention(String bucketName, String objectName, Retention config) {
        setObjectRetention(bucketName, objectName, config, false);
    }

    /**
     * 添加对象的保留配置，存储桶需要设置为对象锁定模式，并且没有开启版本控制，否则会报错收蠕虫保护。
     *
     * @param bucketName           存储桶名称
     * @param objectName           对象名称
     * @param config               保留配置 {@link Retention}
     * @param bypassGovernanceMode 使用 Governance 模式
     */
    public void setObjectRetention(String bucketName, String objectName, Retention config, boolean bypassGovernanceMode) {
        setObjectRetention(bucketName, null, objectName, config, bypassGovernanceMode);
    }

    /**
     * 添加对象的保留配置，存储桶需要设置为对象锁定模式，并且没有开启版本控制，否则会报错收蠕虫保护。
     *
     * @param bucketName           存储桶名称
     * @param region               区域
     * @param objectName           对象名称
     * @param config               保留配置 {@link Retention}
     * @param bypassGovernanceMode 使用 Governance 模式
     */
    public void setObjectRetention(String bucketName, String region, String objectName, Retention config, boolean bypassGovernanceMode) {
        setObjectRetention(bucketName, region, objectName, config, bypassGovernanceMode, null);
    }

    /**
     * 添加对象的保留配置，存储桶需要设置为对象锁定模式，并且没有开启版本控制，否则会报错收蠕虫保护。
     *
     * @param bucketName           存储桶名称
     * @param region               区域
     * @param objectName           对象名称
     * @param config               保留配置 {@link Retention}
     * @param bypassGovernanceMode 使用 Governance 模式
     * @param versionId            版本ID
     */
    public void setObjectRetention(String bucketName, String region, String objectName, Retention config, boolean bypassGovernanceMode, String versionId) {
        setObjectRetention(SetObjectRetentionArgs.builder()
                .bucket(bucketName)
                .region(region)
                .object(objectName)
                .config(config)
                .bypassGovernanceMode(bypassGovernanceMode)
                .versionId(versionId)
                .build());
    }

    /**
     * 添加对象的保留配置，存储桶需要设置为对象锁定模式，并且没有开启版本控制，否则会报错收蠕虫保护。
     *
     * @param setObjectRetentionArgs {@link SetObjectRetentionArgs}
     */
    public void setObjectRetention(SetObjectRetentionArgs setObjectRetentionArgs) {
        String function = "setObjectRetention";
        MinioClient minioClient = getClient();

        try {
            minioClient.setObjectRetention(setObjectRetentionArgs);
        } catch (ErrorResponseException e) {
            log.error("[Goya] |- Minio catch ErrorResponseException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InsufficientDataException e) {
            log.error("[Goya] |- Minio catch InsufficientDataException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InternalException e) {
            log.error("[Goya] |- Minio catch InternalException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InvalidResponseException e) {
            log.error("[Goya] |- Minio catch InvalidResponseException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (ServerException e) {
            log.error("[Goya] |- Minio catch ServerException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (XmlParserException e) {
            log.error("[Goya] |- Minio catch XmlParserException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }
}
