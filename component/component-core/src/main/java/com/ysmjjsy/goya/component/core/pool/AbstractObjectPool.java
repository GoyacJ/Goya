package com.ysmjjsy.goya.component.core.pool;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jspecify.annotations.NonNull;

/**
 * <p>对象池抽象定义</p>
 *
 * @author goya
 * @since 2025/11/1 16:02
 */
public abstract class AbstractObjectPool<T> {

    private final GenericObjectPool<T> genericObjectPool;

    protected AbstractObjectPool(@NonNull PooledObjectFactory<T> pooledObjectFactory, @NonNull Pool pool) {
        GenericObjectPoolConfig<T> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(pool.getMaxTotal());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());
        config.setMaxWait(pool.getMaxWait());
        config.setMinEvictableIdleDuration(pool.getMinEvictableIdleDuration());
        config.setSoftMinEvictableIdleDuration(pool.getSoftMinEvictableIdleDuration());
        config.setLifo(pool.getLifo());
        config.setBlockWhenExhausted(pool.getBlockWhenExhausted());
        genericObjectPool = new GenericObjectPool<>(pooledObjectFactory, config);
    }

    public T get() {
        try {
            return genericObjectPool.borrowObject();
        } catch (Exception e) {
            throw new CommonException("Can not fetch object from pool.", e);
        }
    }

    public void close(T client) {
        if (ObjectUtils.isNotEmpty(client)) {
            genericObjectPool.returnObject(client);
        }
    }
}
