package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioService;
import io.minio.DisableObjectLegalHoldArgs;
import io.minio.EnableObjectLegalHoldArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Object 合法持有</p>
 *
 * @author goya
 * @since 2025/11/1 16:53
 */
@Slf4j
@Service
public class MinioObjectLegalHoldService extends BaseMinioService {

    public MinioObjectLegalHoldService(MinioClientObjectPool minioClientObjectPool) {
        super(minioClientObjectPool);
    }

    /**
     * 启用对对象的合法保留
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    public void enableObjectLegalHold(String bucketName, String objectName) {
        enableObjectLegalHold(bucketName, null, objectName);
    }

    /**
     * 启用对对象的合法保留
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @param objectName 对象名称
     */
    public void enableObjectLegalHold(String bucketName, String region, String objectName) {
        enableObjectLegalHold(bucketName, region, objectName, null);
    }

    /**
     * 启用对对象的合法保留
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @param objectName 对象名称
     * @param versionId  版本ID
     */
    public void enableObjectLegalHold(String bucketName, String region, String objectName, String versionId) {
        enableObjectLegalHold(EnableObjectLegalHoldArgs.builder().bucket(bucketName).region(region).object(objectName).versionId(versionId).build());
    }

    /**
     * 启用对对象的合法保留
     *
     * @param enableObjectLegalHoldArgs {@link EnableObjectLegalHoldArgs}
     */
    public void enableObjectLegalHold(EnableObjectLegalHoldArgs enableObjectLegalHoldArgs) {
        String function = "enableObjectLegalHold";
        MinioClient minioClient = getClient();

        try {
            minioClient.enableObjectLegalHold(enableObjectLegalHoldArgs);
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
     * 禁用对对象的合法保留。
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    public void disableObjectLegalHold(String bucketName, String objectName) {
        disableObjectLegalHold(bucketName, null, objectName);
    }

    /**
     * 禁用对对象的合法保留。
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @param objectName 对象名称
     */
    public void disableObjectLegalHold(String bucketName, String region, String objectName) {
        disableObjectLegalHold(bucketName, region, objectName, null);
    }

    /**
     * 禁用对对象的合法保留。
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @param objectName 对象名称
     * @param versionId  版本ID
     */
    public void disableObjectLegalHold(String bucketName, String region, String objectName, String versionId) {
        disableObjectLegalHold(DisableObjectLegalHoldArgs.builder().bucket(bucketName).region(region).object(objectName).versionId(versionId).build());
    }

    /**
     * 禁用对对象的合法保留。
     *
     * @param disableObjectLegalHoldArgs {@link DisableObjectLegalHoldArgs}
     */
    public void disableObjectLegalHold(DisableObjectLegalHoldArgs disableObjectLegalHoldArgs) {
        String function = "disableObjectLegalHold";
        MinioClient minioClient = getClient();

        try {
            minioClient.disableObjectLegalHold(disableObjectLegalHoldArgs);
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
