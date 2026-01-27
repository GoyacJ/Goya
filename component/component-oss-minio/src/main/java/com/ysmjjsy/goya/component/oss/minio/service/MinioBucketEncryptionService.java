package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioService;
import io.minio.DeleteBucketEncryptionArgs;
import io.minio.GetBucketEncryptionArgs;
import io.minio.MinioClient;
import io.minio.SetBucketEncryptionArgs;
import io.minio.errors.*;
import io.minio.messages.SseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Bucket 加密服务</p>
 *
 * @author goya
 * @since 2025/11/1 16:38
 */
@Service
public class MinioBucketEncryptionService extends BaseMinioService {

    private static final Logger log = LoggerFactory.getLogger(MinioBucketEncryptionService.class);

    public MinioBucketEncryptionService(MinioClientObjectPool minioClientObjectPool) {
        super(minioClientObjectPool);
    }

    /**
     * 获取 Bucket 加密配置
     *
     * @param bucketName 存储桶名称
     * @return 自定义 SseConfiguration 枚举 {@link SseConfiguration}
     */
    public SseConfiguration getBucketEncryption(String bucketName) {
        return getBucketEncryption(bucketName, null);
    }

    /**
     * 获取 Bucket 加密配置
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @return 自定义 SseConfiguration 枚举 {@link SseConfiguration}
     */
    public SseConfiguration getBucketEncryption(String bucketName, String region) {
        return getBucketEncryption(GetBucketEncryptionArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 获取 Bucket 加密配置
     *
     * @param getBucketEncryptionArgs {@link GetBucketEncryptionArgs}
     */
    public SseConfiguration getBucketEncryption(GetBucketEncryptionArgs getBucketEncryptionArgs) {
        String function = "getBucketEncryption";
        MinioClient minioClient = getClient();

        try {
            return minioClient.getBucketEncryption(getBucketEncryptionArgs);
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
     * 设置 Bucket 加密
     *
     * @param bucketName 存储桶名称
     * @param config     加密配置 {@link SseConfiguration}
     */
    public void setBucketEncryption(String bucketName, SseConfiguration config) {
        setBucketEncryption(bucketName, null, config);
    }

    /**
     * 设置 Bucket 加密
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @param config     加密配置 {@link SseConfiguration}
     */
    public void setBucketEncryption(String bucketName, String region, SseConfiguration config) {
        setBucketEncryption(SetBucketEncryptionArgs.builder().bucket(bucketName).region(region).config(config).build());
    }


    /**
     * 设置 Bucket 加密
     *
     * @param setBucketEncryptionArgs {@link SetBucketEncryptionArgs}
     */
    public void setBucketEncryption(SetBucketEncryptionArgs setBucketEncryptionArgs) {
        String function = "setBucketEncryption";
        MinioClient minioClient = getClient();

        try {
            minioClient.setBucketEncryption(setBucketEncryptionArgs);
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
     * 删除 Bucket 加密配置
     *
     * @param bucketName 存储桶名称
     */
    public void deleteBucketEncryption(String bucketName) {
        deleteBucketEncryption(bucketName, null);
    }

    /**
     * 删除 Bucket 加密配置
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     */
    public void deleteBucketEncryption(String bucketName, String region) {
        deleteBucketEncryption(DeleteBucketEncryptionArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 删除 Bucket 加密配置
     *
     * @param deleteBucketEncryptionArgs {@link DeleteBucketEncryptionArgs}
     */
    public void deleteBucketEncryption(DeleteBucketEncryptionArgs deleteBucketEncryptionArgs) {
        String function = "deleteBucketEncryption";
        MinioClient minioClient = getClient();

        try {
            minioClient.deleteBucketEncryption(deleteBucketEncryptionArgs);
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
