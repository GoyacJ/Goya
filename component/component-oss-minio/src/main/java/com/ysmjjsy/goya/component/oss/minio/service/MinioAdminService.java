package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioAdminClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioAdminService;
import io.minio.admin.MinioAdminClient;
import io.minio.admin.messages.DataUsageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/11/1 16:37
 */
@Slf4j
@Service
public class MinioAdminService extends BaseMinioAdminService {

    public MinioAdminService(MinioAdminClientObjectPool minioAdminClientObjectPool) {
        super(minioAdminClientObjectPool);
    }

    /**
     * 获取服务器/群集数据使用情况信息
     *
     * @return {@link DataUsageInfo}
     */
    public DataUsageInfo getDataUsageInfo() {
        String function = "getDataUsageInfo";

        MinioAdminClient minioAdminClient = getClient();

        try {
            return minioAdminClient.getDataUsageInfo();
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(minioAdminClient);
        }
    }
}

