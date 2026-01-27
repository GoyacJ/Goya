package com.ysmjjsy.goya.component.oss.minio.definition.pool;

import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.core.client.AbstractOssClientPooledObjectFactory;

/**
 * <p>Minio 异步 Client 对象池</p>
 *
 * @author goya
 * @since 2025/11/1 16:09
 */
public class MinioAsyncClientObjectPool extends AbstractObjectPool<MinioAsyncClient> {

    public MinioAsyncClientObjectPool(AbstractOssClientPooledObjectFactory<MinioAsyncClient> factory) {
        super(factory, factory.getOssProperties().getPool());
    }
}
