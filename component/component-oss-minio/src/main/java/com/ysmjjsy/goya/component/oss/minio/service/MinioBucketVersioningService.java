package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioService;
import io.minio.GetBucketVersioningArgs;
import io.minio.MinioClient;
import io.minio.SetBucketVersioningArgs;
import io.minio.errors.*;
import io.minio.messages.VersioningConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Bucket 版本控制</p>
 * 若开启了多版本控制，上传对象时，OBS自动为每个对象创建唯一的版本号。上传同名的对象将以不同的版本号同时保存在OBS中。
 * 若未开启多版本控制，向同一个文件夹中上传同名的对象时，新上传的对象将覆盖原有的对象。
 * 某些功能（例如版本控制、对象锁定和存储桶复制）需要使用擦除编码分布式部署 MinIO。开启了版本控制后，允许在同一密钥下保留同一对象的多个版本。
 * @author goya
 * @since 2025/11/1 16:48
 */
@Service
public class MinioBucketVersioningService extends BaseMinioService {

    private static final Logger log = LoggerFactory.getLogger(MinioBucketVersioningService.class);

    public MinioBucketVersioningService(MinioClientObjectPool minioClientObjectPool) {
        super(minioClientObjectPool);
    }

    /**
     * 开启 Bucket 版本控制
     *
     * @param bucketName bucketName
     */
    public void enabledBucketVersioning(String bucketName) {
        setBucketVersioning(bucketName, VersioningConfiguration.Status.ENABLED);
    }

    /**
     * 开启 Bucket 版本控制
     *
     * @param bucketName bucketName
     * @param region     region
     */
    public void enabledBucketVersioning(String bucketName, String region) {
        setBucketVersioning(bucketName, region, VersioningConfiguration.Status.ENABLED);
    }

    /**
     * 暂停 Bucket 版本控制
     *
     * @param bucketName bucketName
     */
    public void suspendedBucketVersioning(String bucketName) {
        setBucketVersioning(bucketName, VersioningConfiguration.Status.SUSPENDED);
    }

    /**
     * 暂停 Bucket 版本控制
     *
     * @param bucketName bucketName
     * @param region     region
     */
    public void suspendedBucketVersioning(String bucketName, String region) {
        setBucketVersioning(bucketName, region, VersioningConfiguration.Status.SUSPENDED);
    }

    /**
     * 关闭 Bucket 版本控制
     *
     * @param bucketName bucketName
     */
    public void offBucketVersioning(String bucketName) {
        setBucketVersioning(bucketName, VersioningConfiguration.Status.OFF);
    }

    /**
     * 关闭 Bucket 版本控制
     *
     * @param bucketName bucketName
     * @param region     region
     */
    public void offBucketVersioning(String bucketName, String region) {
        setBucketVersioning(bucketName, region, VersioningConfiguration.Status.OFF);
    }


    /**
     * 设置 Bucket 版本控制
     *
     * @param bucketName bucketName
     * @param status     {@link  VersioningConfiguration.Status}
     */
    public void setBucketVersioning(String bucketName, VersioningConfiguration.Status status) {
        setBucketVersioning(bucketName, status, null);
    }

    /**
     * 设置 Bucket 版本控制
     *
     * @param bucketName bucketName
     * @param status     {@link  VersioningConfiguration.Status}
     * @param mfaDelete  mfaDelete
     */
    public void setBucketVersioning(String bucketName, VersioningConfiguration.Status status, Boolean mfaDelete) {
        setBucketVersioning(bucketName, new VersioningConfiguration(status, mfaDelete));
    }

    /**
     * 设置 Bucket 版本控制
     *
     * @param bucketName              bucketName
     * @param versioningConfiguration {@link VersioningConfiguration}
     */
    public void setBucketVersioning(String bucketName, VersioningConfiguration versioningConfiguration) {
        setBucketVersioning(SetBucketVersioningArgs.builder().bucket(bucketName).config(versioningConfiguration).build());
    }

    /**
     * 设置 Bucket 版本控制
     *
     * @param bucketName bucketName
     * @param region     region
     * @param status     {@link  VersioningConfiguration.Status}
     */
    public void setBucketVersioning(String bucketName, String region, VersioningConfiguration.Status status) {
        setBucketVersioning(bucketName, region, status, null);
    }

    /**
     * 设置 Bucket 版本控制
     *
     * @param bucketName bucketName
     * @param region     region
     * @param status     {@link  VersioningConfiguration.Status}
     * @param mfaDelete  mfaDelete
     */
    public void setBucketVersioning(String bucketName, String region, VersioningConfiguration.Status status, Boolean mfaDelete) {
        setBucketVersioning(bucketName, region, new VersioningConfiguration(status, mfaDelete));
    }

    /**
     * 设置 Bucket 版本控制
     *
     * @param bucketName              bucketName
     * @param region                  region
     * @param versioningConfiguration {@link VersioningConfiguration}
     */
    public void setBucketVersioning(String bucketName, String region, VersioningConfiguration versioningConfiguration) {
        setBucketVersioning(SetBucketVersioningArgs.builder().bucket(bucketName).region(region).config(versioningConfiguration).build());
    }

    /**
     * 设置 Bucket 版本控制
     *
     * @param setBucketVersioningArgs {@link SetBucketVersioningArgs}
     */
    public void setBucketVersioning(SetBucketVersioningArgs setBucketVersioningArgs) {
        String function = "setBucketVersioning";
        MinioClient minioClient = getClient();

        try {
            minioClient.setBucketVersioning(setBucketVersioningArgs);
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
     * 获取 Bucket 版本配置
     *
     * @param bucketName bucketName
     * @return {@link VersioningConfiguration}
     */
    public VersioningConfiguration getBucketVersioning(String bucketName) {
        return getBucketVersioning(GetBucketVersioningArgs.builder().bucket(bucketName).build());
    }

    /**
     * 获取 Bucket 版本配置
     *
     * @param bucketName bucketName
     * @param region     region
     * @return {@link VersioningConfiguration}
     */
    public VersioningConfiguration getBucketVersioning(String bucketName, String region) {
        return getBucketVersioning(GetBucketVersioningArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 获取 Bucket 版本配置
     *
     * @param getBucketVersioningArgs {@link GetBucketVersioningArgs}
     * @return {@link VersioningConfiguration}
     */
    public VersioningConfiguration getBucketVersioning(GetBucketVersioningArgs getBucketVersioningArgs) {
        String function = "getBucketVersioning";
        MinioClient minioClient = getClient();

        try {
            return minioClient.getBucketVersioning(getBucketVersioningArgs);
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
            log.error("[Goya] |- Minio catch XmlParserException [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }
}
