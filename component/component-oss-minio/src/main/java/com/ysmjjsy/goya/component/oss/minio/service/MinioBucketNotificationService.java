package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioService;
import io.minio.DeleteBucketNotificationArgs;
import io.minio.GetBucketNotificationArgs;
import io.minio.MinioClient;
import io.minio.SetBucketNotificationArgs;
import io.minio.errors.*;
import io.minio.messages.NotificationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Bucket 通知配置</p>
 *
 * @author goya
 * @since 2025/11/1 16:41
 */
@Slf4j
@Service
public class MinioBucketNotificationService extends BaseMinioService {

    public MinioBucketNotificationService(MinioClientObjectPool minioClientObjectPool) {
        super(minioClientObjectPool);
    }

    /**
     * 设置 Bucket 通知
     *
     * @param bucketName                bucketName
     * @param notificationConfiguration {@link NotificationConfiguration}
     */
    public void setBucketNotification(String bucketName, NotificationConfiguration notificationConfiguration) {
        setBucketNotification(SetBucketNotificationArgs.builder().bucket(bucketName).config(notificationConfiguration).build());
    }

    /**
     * 设置 Bucket 通知
     *
     * @param bucketName                bucketName
     * @param region                    region
     * @param notificationConfiguration {@link NotificationConfiguration}
     */
    public void setBucketNotification(String bucketName, String region, NotificationConfiguration notificationConfiguration) {
        setBucketNotification(SetBucketNotificationArgs.builder().bucket(bucketName).region(region).config(notificationConfiguration).build());
    }


    /**
     * 设置 Bucket 通知
     *
     * @param setBucketNotificationArgs {@link SetBucketNotificationArgs}
     */
    public void setBucketNotification(SetBucketNotificationArgs setBucketNotificationArgs) {
        String function = "setBucketNotification";
        MinioClient minioClient = getClient();

        try {
            minioClient.setBucketNotification(setBucketNotificationArgs);
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
     * 获取 Bucket 通知配置
     *
     * @param bucketName bucketName
     * @return {@link  NotificationConfiguration}
     */
    public NotificationConfiguration getBucketNotification(String bucketName) {
        return getBucketNotification(GetBucketNotificationArgs.builder().bucket(bucketName).build());
    }

    /**
     * 获取 Bucket 通知配置
     *
     * @param bucketName bucketName
     * @param region     region
     * @return {@link  NotificationConfiguration}
     */
    public NotificationConfiguration getBucketNotification(String bucketName, String region) {
        return getBucketNotification(GetBucketNotificationArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 获取 Bucket 通知配置
     *
     * @param getBucketNotificationArgs {@link GetBucketNotificationArgs}
     * @return {@link  NotificationConfiguration}
     */
    public NotificationConfiguration getBucketNotification(GetBucketNotificationArgs getBucketNotificationArgs) {
        String function = "getBucketNotification";
        MinioClient minioClient = getClient();

        try {
            return minioClient.getBucketNotification(getBucketNotificationArgs);
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
            log.error("[Goya] |- Minio catch XmlParserException in createBucket.", e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }

    /**
     * 删除 Bucket 通知配置
     *
     * @param bucketName bucketName
     */
    public void deleteBucketNotification(String bucketName) {
        deleteBucketNotification(DeleteBucketNotificationArgs.builder().bucket(bucketName).build());
    }

    /**
     * 删除 Bucket 通知配置
     *
     * @param bucketName bucketName
     * @param region     region
     */
    public void deleteBucketNotification(String bucketName, String region) {
        deleteBucketNotification(DeleteBucketNotificationArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 删除 Bucket 通知配置
     *
     * @param deleteBucketNotificationArgs {@link DeleteBucketNotificationArgs}
     */
    public void deleteBucketNotification(DeleteBucketNotificationArgs deleteBucketNotificationArgs) {
        String function = "deleteBucketNotification";
        MinioClient minioClient = getClient();

        try {
            minioClient.deleteBucketNotification(deleteBucketNotificationArgs);
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
            log.error("[Goya] |- Minio catch XmlParserException in createBucket.", e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }
}