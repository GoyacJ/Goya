package com.ysmjjsy.goya.component.oss.minio.definition.service;

import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.framework.oss.service.BaseOssService;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.MinioAsyncClient;

/**
 * <p>Minio 基础异步服务</p>
 *
 * @author goya
 * @since 2025/11/1 16:11
 */
public abstract class BaseMinioAsyncService extends BaseOssService<MinioAsyncClient> {

    protected BaseMinioAsyncService(AbstractObjectPool<MinioAsyncClient> ossClientObjectPool) {
        super(ossClientObjectPool);
    }
}
