package com.ysmjjsy.goya.component.oss.s3.definition.pool;

import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.framework.oss.client.AbstractOssClientPooledObjectFactory;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * <p>Amazon S3 Client 对象池 </p>
 *
 * @author goya
 * @since 2023/7/14 16:33
 */
public class S3ClientObjectPool extends AbstractObjectPool<S3Client> {

    public S3ClientObjectPool(AbstractOssClientPooledObjectFactory<S3Client> factory) {
        super(factory, factory.getOssProperties().getPool());
    }
}
