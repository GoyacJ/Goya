package com.ysmjjsy.goya.component.oss.aliyun.definition.pool;

import com.aliyun.oss.OSS;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.core.client.AbstractOssClientPooledObjectFactory;

/**
 * <p>Amazon S3 Client 对象池 </p>
 *
 * @author goya
 * @since 2023/7/14 16:33
 */
public class AliyunClientObjectPool extends AbstractObjectPool<OSS> {

    public AliyunClientObjectPool(AbstractOssClientPooledObjectFactory<OSS> factory) {
        super(factory, factory.getOssProperties().getPool());
    }
}
