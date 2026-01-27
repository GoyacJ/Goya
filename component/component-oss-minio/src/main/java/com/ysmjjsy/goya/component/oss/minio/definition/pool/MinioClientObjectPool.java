package com.ysmjjsy.goya.component.oss.minio.definition.pool;

import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.framework.oss.client.AbstractOssClientPooledObjectFactory;
import io.minio.MinioClient;

/**
 * <p>Minio 客户端连接池</p>
 *
 * @author goya
 * @since 2025/11/1 16:10
 */
public class MinioClientObjectPool extends AbstractObjectPool<MinioClient> {

    public MinioClientObjectPool(AbstractOssClientPooledObjectFactory<MinioClient> factory) {
        super(factory, factory.getOssProperties().getPool());
    }
}