package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioAdminClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioAdminService;
import com.ysmjjsy.goya.component.oss.minio.domain.policy.PolicyDomain;
import io.minio.admin.MinioAdminClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/11/1 16:36
 */
@Slf4j
@Service
public class MinioAdminPolicyService extends BaseMinioAdminService {

    public MinioAdminPolicyService(MinioAdminClientObjectPool minioAdminClientObjectPool) {
        super(minioAdminClientObjectPool);
    }

    /**
     * 获取屏蔽策略列表
     *
     * @return 屏蔽策略列表
     */
    public Map<String, String> listCannedPolicies() {
        String function = "listCannedPolicies";

        MinioAdminClient minioAdminClient = getClient();

        try {
            return minioAdminClient.listCannedPolicies();
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

    /**
     * 创建屏蔽策略
     *
     * @param name   策略名称
     * @param policy 策略 {@link PolicyDomain}
     */
    public void addCannedPolicy(String name, String policy) {
        String function = "addCannedPolicy";

        MinioAdminClient minioAdminClient = getClient();

        try {
            minioAdminClient.addCannedPolicy(name, policy);
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

    /**
     * 移除屏蔽策略
     *
     * @param name 策略名称
     */
    public void removeCannedPolicy(String name) {
        String function = "removeCannedPolicy";

        MinioAdminClient minioAdminClient = getClient();

        try {
            minioAdminClient.removeCannedPolicy(name);
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

    /**
     * 设置屏蔽策略
     *
     * @param userOrGroupName 用户名或组名
     * @param isGroup         是否是组
     * @param policyName      策略名称
     */
    public void setPolicy(String userOrGroupName, boolean isGroup, String policyName) {
        String function = "setPolicy";

        MinioAdminClient minioAdminClient = getClient();

        try {
            minioAdminClient.setPolicy(userOrGroupName, isGroup, policyName);
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
