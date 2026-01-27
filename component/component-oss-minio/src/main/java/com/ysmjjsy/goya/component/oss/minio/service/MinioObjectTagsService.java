package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioService;
import io.minio.DeleteObjectTagsArgs;
import io.minio.GetObjectTagsArgs;
import io.minio.MinioClient;
import io.minio.SetObjectTagsArgs;
import io.minio.errors.*;
import io.minio.messages.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Minio 对象标签服务</p>
 *
 * @author goya
 * @since 2025/11/1 17:02
 */
@Slf4j
@Service
public class MinioObjectTagsService extends BaseMinioService {

    public MinioObjectTagsService(MinioClientObjectPool minioClientObjectPool) {
        super(minioClientObjectPool);
    }

    /**
     * 获取对象的标签
     *
     * @param bucketName bucketName
     * @param objectName objectName
     * @return {@link Tags}
     */
    public Tags getObjectTags(String bucketName, String objectName) {
        return getObjectTags(bucketName, null, objectName);
    }

    /**
     * 获取对象的标签
     *
     * @param bucketName bucketName
     * @param objectName objectName
     * @param region     region
     * @return {@link Tags}
     */
    public Tags getObjectTags(String bucketName, String region, String objectName) {
        return getObjectTags(bucketName, region, objectName, null);
    }

    /**
     * 获取对象的标签
     *
     * @param bucketName bucketName
     * @param objectName objectName
     * @param region     region
     * @param versionId  versionId
     * @return {@link Tags}
     */
    public Tags getObjectTags(String bucketName, String region, String objectName, String versionId) {
        return getObjectTags(GetObjectTagsArgs.builder().bucket(bucketName).object(objectName).region(region).versionId(versionId).build());
    }

    /**
     * 获取对象的标签。
     *
     * @param getObjectTagsArgs {@link GetObjectTagsArgs}
     * @return {@link Tags}
     */
    public Tags getObjectTags(GetObjectTagsArgs getObjectTagsArgs) {
        String function = "getObjectTags";
        MinioClient minioClient = getClient();

        try {
            return minioClient.getObjectTags(getObjectTagsArgs);
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
     * 为对象设置标签
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param tags       标签 {@link Tags}
     */
    public void setObjectTags(String bucketName, String objectName, Tags tags) {
        setObjectTags(bucketName, null, objectName, tags);
    }

    /**
     * 为对象设置标签
     *
     * @param bucketName 存储桶名称
     * @param region     存储桶区域
     * @param objectName 对象名称
     * @param tags       标签 {@link Tags}
     */
    public void setObjectTags(String bucketName, String region, String objectName, Tags tags) {
        setObjectTags(bucketName, region, objectName, tags, null);
    }

    /**
     * 为对象设置标签
     *
     * @param bucketName 存储桶名称
     * @param region     存储桶区域
     * @param objectName 对象名称
     * @param tags       标签 {@link Tags}
     * @param versionId  版本ID
     */
    public void setObjectTags(String bucketName, String region, String objectName, Tags tags, String versionId) {
        setObjectTags(SetObjectTagsArgs.builder().bucket(bucketName).object(objectName).region(region).versionId(versionId).tags(tags).build());
    }

    /**
     * 为对象设置标签
     *
     * @param setObjectTagsArgs {@link SetObjectTagsArgs}
     */
    public void setObjectTags(SetObjectTagsArgs setObjectTagsArgs) {
        String function = "setObjectTags";
        MinioClient minioClient = getClient();

        try {
            minioClient.setObjectTags(setObjectTagsArgs);
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
     * 清空对象设置标签
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    public void deleteObjectTags(String bucketName, String objectName) {
        deleteObjectTags(bucketName, null, objectName);
    }

    /**
     * 清空对象设置标签
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @param objectName 对象名称
     */
    public void deleteObjectTags(String bucketName, String region, String objectName) {
        deleteObjectTags(bucketName, region, objectName, null);
    }


    /**
     * 清空对象设置标签
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @param objectName 对象名称
     * @param versionId  版本ID
     */
    public void deleteObjectTags(String bucketName, String region, String objectName, String versionId) {
        deleteObjectTags(DeleteObjectTagsArgs.builder().bucket(bucketName).object(objectName).region(region).versionId(versionId).build());
    }

    /**
     * 清空对象设置标签
     *
     * @param deleteObjectTagsArgs {@link DeleteObjectTagsArgs}
     */
    public void deleteObjectTags(DeleteObjectTagsArgs deleteObjectTagsArgs) {
        String function = "deleteObjectTags";
        MinioClient minioClient = getClient();

        try {
            minioClient.deleteObjectTags(deleteObjectTagsArgs);
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
