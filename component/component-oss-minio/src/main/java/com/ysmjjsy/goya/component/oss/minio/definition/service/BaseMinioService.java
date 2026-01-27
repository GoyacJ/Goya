package com.ysmjjsy.goya.component.oss.minio.definition.service;

import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.framework.oss.service.BaseOssService;
import io.minio.MinioClient;

/**
 * <p>Minio 基础服务</p>
 *
 * @author goya
 * @since 2025/11/1 16:11
 */
public abstract class BaseMinioService extends BaseOssService<MinioClient> {

    protected BaseMinioService(AbstractObjectPool<MinioClient> ossClientObjectPool) {
        super(ossClientObjectPool);
    }
}
