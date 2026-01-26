package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioService;
import io.minio.DeleteBucketLifecycleArgs;
import io.minio.GetBucketLifecycleArgs;
import io.minio.MinioClient;
import io.minio.SetBucketLifecycleArgs;
import io.minio.errors.*;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * <p>Bucket 生命周期配置服务</p>
 * 生命周期管理可适用于以下典型场景:
 *      周期性上传的日志文件，可能只需要保留一个星期或一个月。到期后要删除它们。
 *      某些文档在一段时间内经常访问，但是超过一定时间后便可能不再访问了。这些文档需要在一定时间后转化为低频访问存储，归档存储或者删除
 * @author goya
 * @since 2025/11/1 16:39
 */
@Slf4j
@Service
public class MinioBucketLifecycleService extends BaseMinioService {

    public MinioBucketLifecycleService(MinioClientObjectPool minioClientObjectPool) {
        super(minioClientObjectPool);
    }

    /**
     * 设置 Bucket 生命周期配置
     *
     * @param bucketName     bucketName
     * @param lifecycleRules {@link LifecycleRule}
     */
    public void setBucketLifecycle(String bucketName, List<LifecycleRule> lifecycleRules) {
        setBucketLifecycle(bucketName, new LifecycleConfiguration(lifecycleRules));
    }

    /**
     * 置 Bucket 生命周期配置
     *
     * @param bucketName     bucketName
     * @param region         region
     * @param lifecycleRules {@link LifecycleRule}
     */
    public void setBucketLifecycle(String bucketName, String region, List<LifecycleRule> lifecycleRules) {
        setBucketLifecycle(bucketName, region, new LifecycleConfiguration(lifecycleRules));
    }

    /**
     * 设置 Bucket 生命周期
     *
     * @param bucketName             bucketName
     * @param lifecycleConfiguration {@link LifecycleConfiguration}
     */
    public void setBucketLifecycle(String bucketName, LifecycleConfiguration lifecycleConfiguration) {
        setBucketLifecycle(SetBucketLifecycleArgs.builder().bucket(bucketName).config(lifecycleConfiguration).build());
    }

    /**
     * 设置 Bucket 生命周期
     *
     * @param bucketName             bucketName
     * @param region                 region
     * @param lifecycleConfiguration @link LifecycleConfiguration}
     */
    public void setBucketLifecycle(String bucketName, String region, LifecycleConfiguration lifecycleConfiguration) {
        setBucketLifecycle(SetBucketLifecycleArgs.builder().bucket(bucketName).region(region).config(lifecycleConfiguration).build());
    }

    /**
     * 设置 Bucket 生命周期
     *
     * @param setBucketLifecycleArgs {@link SetBucketLifecycleArgs}
     */
    public void setBucketLifecycle(SetBucketLifecycleArgs setBucketLifecycleArgs) {
        String function = "setBucketLifecycle";
        MinioClient minioClient = getClient();

        try {
            minioClient.setBucketLifecycle(setBucketLifecycleArgs);
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
     * 获取 Bucket 生命周期配置
     *
     * @param bucketName bucketName
     * @return {@link LifecycleConfiguration}
     */
    public LifecycleConfiguration getBucketLifecycle(String bucketName) {
        return getBucketLifecycle(GetBucketLifecycleArgs.builder().bucket(bucketName).build());
    }

    /**
     * 获取 Bucket 生命周期配置
     *
     * @param bucketName bucketName
     * @param region     region
     * @return {@link LifecycleConfiguration}
     */
    public LifecycleConfiguration getBucketLifecycle(String bucketName, String region) {
        return getBucketLifecycle(GetBucketLifecycleArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 获取 Bucket 生命周期配置
     *
     * @param getBucketLifecycleArgs {@link GetBucketLifecycleArgs}
     */
    public LifecycleConfiguration getBucketLifecycle(GetBucketLifecycleArgs getBucketLifecycleArgs) {
        String function = "getBucketLifecycle";
        MinioClient minioClient = getClient();

        try {
            return minioClient.getBucketLifecycle(getBucketLifecycleArgs);
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
     * 删除 Bucket 生命周期配置
     *
     * @param bucketName bucketName
     */
    public void deleteBucketLifecycle(String bucketName) {
        deleteBucketLifecycle(DeleteBucketLifecycleArgs.builder().bucket(bucketName).build());
    }

    /**
     * 删除 Bucket 生命周期配置
     *
     * @param bucketName bucketName
     * @param region     region
     */
    public void deleteBucketLifecycle(String bucketName, String region) {
        deleteBucketLifecycle(DeleteBucketLifecycleArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 删除 Bucket 生命周期配置
     *
     * @param deleteBucketLifecycleArgs {@link DeleteBucketLifecycleArgs}
     */
    public void deleteBucketLifecycle(DeleteBucketLifecycleArgs deleteBucketLifecycleArgs) {
        String function = "deleteBucketLifecycle";
        MinioClient minioClient = getClient();

        try {
            minioClient.deleteBucketLifecycle(deleteBucketLifecycleArgs);
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
