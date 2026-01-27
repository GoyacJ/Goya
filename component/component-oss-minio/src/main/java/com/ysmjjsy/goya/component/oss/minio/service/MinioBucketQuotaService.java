package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioAdminClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioAdminService;
import io.minio.admin.MinioAdminClient;
import io.minio.admin.QuotaUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Minio User 管理服务</p>
 *
 * @author goya
 * @since 2025/11/1 16:43
 */
@Slf4j
@Service
public class MinioBucketQuotaService extends BaseMinioAdminService {

    public MinioBucketQuotaService(MinioAdminClientObjectPool minioAdminClientObjectPool) {
        super(minioAdminClientObjectPool);
    }

    /**
     * 设置存储桶配额
     *
     * @param bucketName 存储桶名称
     * @param size       配额大小
     * @param unit       配额单位
     */
    public void setBucketQuota(String bucketName, long size, QuotaUnit unit) {
        String function = "setBucketQuota";

        MinioAdminClient minioAdminClient = getClient();

        try {
            minioAdminClient.setBucketQuota(bucketName, size, unit);
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioAdminClient);
        }
    }

    /**
     * 清除存储桶配额
     *
     * @param bucketName 存储桶名称
     */
    public void clearBucketQuota(String bucketName) {
        setBucketQuota(bucketName, 0, QuotaUnit.KB);
    }

    /**
     * 获取存储桶配额大小
     *
     * @param bucketName 存储桶名称
     * @return 配额大小
     */
    public long getBucketQuota(String bucketName) {
        String function = "getBucketQuota";

        MinioAdminClient minioAdminClient = getClient();

        try {
            return minioAdminClient.getBucketQuota(bucketName);
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioAdminClient);
        }
    }

}