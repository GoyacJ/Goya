package com.ysmjjsy.goya.component.oss.minio.definition.pool;

import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.core.client.AbstractOssClientPooledObjectFactory;
import io.minio.admin.MinioAdminClient;

/**
 * <p>Minio Admin Client 对象池</p>
 *
 * @author goya
 * @since 2025/11/1 16:00
 */
public class MinioAdminClientObjectPool extends AbstractObjectPool<MinioAdminClient> {

    public MinioAdminClientObjectPool(AbstractOssClientPooledObjectFactory<MinioAdminClient> factory) {
        super(factory, factory.getOssProperties().getPool());
    }
}
