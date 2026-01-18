package com.ysmjjsy.goya.component.oss.core.client;

import com.ysmjjsy.goya.component.oss.core.properties.AbstractOssProperties;
import lombok.Getter;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * <p>对象存储 Client 对象池对象工厂抽象定义</p>
 *
 * @author goya
 * @since 2025/11/1 16:05
 */
@Getter
public abstract class AbstractOssClientPooledObjectFactory<T> extends BasePooledObjectFactory<T> {

    private final AbstractOssProperties ossProperties;

    protected AbstractOssClientPooledObjectFactory(AbstractOssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public PooledObject<T> wrap(T obj) {
        return new DefaultPooledObject<>(obj);
    }
}
