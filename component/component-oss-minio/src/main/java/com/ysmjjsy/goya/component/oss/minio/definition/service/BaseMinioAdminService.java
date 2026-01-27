package com.ysmjjsy.goya.component.oss.minio.definition.service;

import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.core.service.BaseOssService;
import io.minio.admin.MinioAdminClient;

/**
 * <p>Minio Admin 基础服务</p>
 *
 * @author goya
 * @since 2025/11/1 16:10
 */
public abstract class BaseMinioAdminService extends BaseOssService<MinioAdminClient> {

    protected BaseMinioAdminService(AbstractObjectPool<MinioAdminClient> ossClientObjectPool) {
        super(ossClientObjectPool);
    }
}

