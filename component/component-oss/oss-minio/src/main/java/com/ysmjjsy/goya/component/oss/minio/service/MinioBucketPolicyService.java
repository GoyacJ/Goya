package com.ysmjjsy.goya.component.oss.minio.service;

import com.google.common.base.Enums;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioService;
import com.ysmjjsy.goya.component.oss.minio.enums.PolicyEnums;
import io.minio.DeleteBucketPolicyArgs;
import io.minio.GetBucketPolicyArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Bucket 访问策略</p>
 *
 * @author goya
 * @since 2025/11/1 16:42
 */
@Slf4j
@Service
public class MinioBucketPolicyService extends BaseMinioService {

    public MinioBucketPolicyService(MinioClientObjectPool minioClientObjectPool) {
        super(minioClientObjectPool);
    }

    /**
     * 获取 Bucket 访问策略配置
     *
     * @param bucketName 存储桶名称
     * @return 自定义策略枚举 {@link PolicyEnums}
     */
    public PolicyEnums getBucketPolicy(String bucketName) {
        return getBucketPolicy(bucketName, null);
    }

    /**
     * 获取 Bucket 访问策略配置
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @return 自定义策略枚举 {@link PolicyEnums}
     */
    public PolicyEnums getBucketPolicy(String bucketName, String region) {
        return getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 获取 Bucket 访问策略配置
     *
     * @param getBucketPolicyArgs {@link GetBucketPolicyArgs}
     */
    public PolicyEnums getBucketPolicy(GetBucketPolicyArgs getBucketPolicyArgs) {
        String function = "getBucketPolicy";
        MinioClient minioClient = getClient();

        try {
            String policy = minioClient.getBucketPolicy(getBucketPolicyArgs);
            if (StringUtils.isNotBlank(policy)) {
                return Enums.getIfPresent(PolicyEnums.class, policy).or(PolicyEnums.PRIVATE);
            } else {
                return PolicyEnums.PRIVATE;
            }
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
        } catch (BucketPolicyTooLargeException e) {
            log.error("[Goya] |- Minio catch BucketPolicyTooLargeException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }

    /**
     * 设置 Bucket 访问策略
     *
     * @param bucketName 存储桶名称
     * @param config     策略配置
     */
    public void setBucketPolicy(String bucketName, String config) {
        setBucketPolicy(bucketName, null, config);
    }

    /**
     * 设置 Bucket 访问策略
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @param config     策略配置
     */
    public void setBucketPolicy(String bucketName, String region, String config) {
        setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).region(region).config(config).build());
    }

    /**
     * 设置 Bucket 访问策略
     *
     * @param setBucketPolicyArgs {@link SetBucketPolicyArgs}
     */
    public void setBucketPolicy(SetBucketPolicyArgs setBucketPolicyArgs) {
        String function = "setBucketPolicy";
        MinioClient minioClient = getClient();

        try {
            minioClient.setBucketPolicy(setBucketPolicyArgs);
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
            log.error("[Goya] |- Minio catch XmlParserException in createBucket.", e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }

    /**
     * 删除 Bucket 访问策略
     *
     * @param bucketName 存储桶名称
     */
    public void deleteBucketPolicy(String bucketName) {
        deleteBucketPolicy(bucketName, null);
    }

    /**
     * 删除 Bucket 访问策略
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     */
    public void deleteBucketPolicy(String bucketName, String region) {
        deleteBucketPolicy(DeleteBucketPolicyArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 删除 Bucket 访问策略
     *
     * @param deleteBucketPolicyArgs {@link DeleteBucketPolicyArgs}
     */
    public void deleteBucketPolicy(DeleteBucketPolicyArgs deleteBucketPolicyArgs) {
        String function = "deleteBucketPolicy";
        MinioClient minioClient = getClient();

        try {
            minioClient.deleteBucketPolicy(deleteBucketPolicyArgs);
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
            log.error("[Goya] |- Minio catch XmlParserException in createBucket.", e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }
}
