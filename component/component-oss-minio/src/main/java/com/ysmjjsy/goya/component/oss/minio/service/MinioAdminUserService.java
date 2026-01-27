package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioAdminClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioAdminService;
import io.minio.admin.MinioAdminClient;
import io.minio.admin.UserInfo;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * <p>Minio 用户管理 Service</p>
 *
 * @author goya
 * @since 2025/11/1 16:37
 */
@Service
public class MinioAdminUserService extends BaseMinioAdminService {

    private static final Logger log = LoggerFactory.getLogger(MinioAdminUserService.class);

    public MinioAdminUserService(MinioAdminClientObjectPool minioAdminClientObjectPool) {
        super(minioAdminClientObjectPool);
    }

    /**
     * 获取 Minio 用户列表
     *
     * @return Map<String, UserInfo>
     */
    public Map<String, UserInfo> listUsers() {
        String function = "listUsers";

        MinioAdminClient minioAdminClient = getClient();

        try {
            return minioAdminClient.listUsers();
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidCipherTextException e) {
            log.error("[Goya] |- Minio catch InvalidCipherTextException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioAdminClient);
        }
    }

    /**
     * 获取指定MinIO用户的用户信息
     *
     * @param accessKey 访问密钥
     * @return {@link UserInfo}
     */
    public UserInfo getUserInfo(String accessKey) {
        String function = "getUserInfo";

        MinioAdminClient minioAdminClient = getClient();

        try {
            return minioAdminClient.getUserInfo(accessKey);
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

    public void addUser(String accessKey, UserInfo.Status status, String secretKey, String policyName, List<String> memberOf) {
        String function = "addUser";

        MinioAdminClient minioAdminClient = getClient();

        try {
            minioAdminClient.addUser(accessKey, status, secretKey, policyName, memberOf);
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- Minio catch NoSuchAlgorithmException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[Goya] |- Minio catch InvalidKeyException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (IOException e) {
            log.error("[Goya] |- Minio catch IOException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (InvalidCipherTextException e) {
            log.error("[Goya] |- Minio catch InvalidCipherTextException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(minioAdminClient);
        }
    }

    /**
     * 通过用户的访问密钥删除用户
     *
     * @param accessKey 访问密钥
     */
    public void deleteUser(String accessKey) {
        String function = "deleteUser";

        MinioAdminClient minioAdminClient = getClient();

        try {
            minioAdminClient.deleteUser(accessKey);
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
