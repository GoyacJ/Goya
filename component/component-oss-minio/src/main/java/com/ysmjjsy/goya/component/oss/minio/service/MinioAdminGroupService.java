package com.ysmjjsy.goya.component.oss.minio.service;

import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioAdminClientObjectPool;
import com.ysmjjsy.goya.component.oss.minio.definition.service.BaseMinioAdminService;
import io.minio.admin.GroupInfo;
import io.minio.admin.MinioAdminClient;
import io.minio.admin.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/11/1 16:35
 */
@Slf4j
@Service
public class MinioAdminGroupService extends BaseMinioAdminService {

    public MinioAdminGroupService(MinioAdminClientObjectPool minioAdminClientObjectPool) {
        super(minioAdminClientObjectPool);
    }

    /**
     * 获取所有MinIO组的列表
     *
     * @return 组列表
     */
    public List<String> listGroups() {
        String function = "listGroups";

        MinioAdminClient minioAdminClient = getClient();

        try {
            return minioAdminClient.listGroups();
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
     * 获取指定MinIO组的组信息
     *
     * @param group 组
     * @return 组信息
     */
    public GroupInfo getGroupInfo(String group) {
        String function = "getGroupInfo";

        MinioAdminClient minioAdminClient = getClient();

        try {
            return minioAdminClient.getGroupInfo(group);
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
     * 添加或者更新 Group
     *
     * @param group       用户组名称
     * @param groupStatus 用户组状态
     * @param members     组内成员
     */
    public void addUpdateGroup(String group, Status groupStatus, List<String> members) {
        String function = "addUpdateGroup";

        MinioAdminClient minioAdminClient = getClient();

        try {
            minioAdminClient.addUpdateGroup(group, groupStatus, members);
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
     * 移除组
     *
     * @param group 组名称
     */
    public void removeGroup(String group) {
        String function = "removeGroup";

        MinioAdminClient minioAdminClient = getClient();

        try {
            minioAdminClient.removeGroup(group);
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