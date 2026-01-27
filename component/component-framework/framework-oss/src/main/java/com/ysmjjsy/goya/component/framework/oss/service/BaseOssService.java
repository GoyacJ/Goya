package com.ysmjjsy.goya.component.framework.oss.service;

import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;

/**
 * <p>对象存储 Service 抽象定义</p>
 *
 * @author goya
 * @since 2025/11/1 16:11
 */
public abstract class BaseOssService<T> {

    private final AbstractObjectPool<T> objectPool;

    protected BaseOssService(AbstractObjectPool<T> objectPool) {
        this.objectPool = objectPool;
    }

    protected T getClient() {
        return objectPool.get();
    }

    protected void close(T client) {
        objectPool.close(client);
    }
}

