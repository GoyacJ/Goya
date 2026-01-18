package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Minio Bucket 存储通基础操作服务</p>
 *
 * @author goya
 * @since 2025/11/1 16:45
 */
@Slf4j
@Service
public class MinioBucketService extends BaseMinioService {

    public MinioBucketService(MinioClientObjectPool minioClientObjectPool) {
        super(minioClientObjectPool);
    }

    /**
     * 查询所有存储桶
     *
     * @return Bucket 列表
     */
    public List<Bucket> listBuckets() {
        return listBuckets(null);
    }

    /**
     * 查询所有存储桶
     *
     * @param args {@link ListBucketsArgs}
     * @return Bucket 列表
     */
    public List<Bucket> listBuckets(ListBucketsArgs args) {
        String function = "listBuckets";
        MinioClient minioClient = getClient();

        try {
            List<Bucket> buckets = new ArrayList<>();
            if (ObjectUtils.isNotEmpty(args)) {
                Iterable<Result<Bucket>> iterable = minioClient.listBuckets(args);
                for (Result<Bucket> result : iterable) {
                    buckets.add(result.get());
                }
            } else {
                buckets = minioClient.listBuckets();
            }
            return buckets;
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
            log.error("[Goya] |- Minio catch XmlParserException in in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }

    /**
     * 存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return 是否存在，true 存在，false 不存在
     */
    public boolean bucketExists(String bucketName) {
        return bucketExists(bucketName, null);
    }

    /**
     * 存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     * @return 是否存在，true 存在，false 不存在
     */
    public boolean bucketExists(String bucketName, String region) {
        return bucketExists(BucketExistsArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 存储桶是否存在
     *
     * @param bucketExistsArgs {@link BucketExistsArgs}
     * @return true 存在，false 不存在
     */
    public boolean bucketExists(BucketExistsArgs bucketExistsArgs) {
        String function = "bucketExists";
        MinioClient minioClient = getClient();

        try {
            return minioClient.bucketExists(bucketExistsArgs);
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
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    public void makeBucket(String bucketName) {
        makeBucket(bucketName, null);
    }

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     */
    public void makeBucket(String bucketName, String region) {
        makeBucket(MakeBucketArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 创建存储桶
     * <p>
     * 该方法仅仅是 Minio 原始方法的封装，不包含校验等操作。
     *
     * @param makeBucketArgs {@link MakeBucketArgs}
     */
    public void makeBucket(MakeBucketArgs makeBucketArgs) {
        String function = "makeBucket";
        MinioClient minioClient = getClient();

        try {
            minioClient.makeBucket(makeBucketArgs);
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
     * 删除一个空的存储桶 如果存储桶存在对象不为空时，删除会报错。
     *
     * @param bucketName 存储桶名称
     */
    public void removeBucket(String bucketName) {
        removeBucket(bucketName, null);
    }

    /**
     * 删除一个空的存储桶 如果存储桶存在对象不为空时，删除会报错。
     *
     * @param bucketName 存储桶名称
     * @param region     区域
     */
    public void removeBucket(String bucketName, String region) {
        removeBucket(RemoveBucketArgs.builder().bucket(bucketName).region(region).build());
    }

    /**
     * 删除一个空的存储桶 如果存储桶存在对象不为空时，删除会报错。
     *
     * @param removeBucketArgs {@link RemoveBucketArgs}
     */
    public void removeBucket(RemoveBucketArgs removeBucketArgs) {
        String function = "removeBucket";
        MinioClient minioClient = getClient();

        try {
            minioClient.removeBucket(removeBucketArgs);
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
            log.error("[Goya] |- Minio catch XmlParserException in in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioClient);
        }
    }
}