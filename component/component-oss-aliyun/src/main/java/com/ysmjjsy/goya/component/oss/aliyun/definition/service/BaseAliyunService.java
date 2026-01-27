package com.ysmjjsy.goya.component.oss.aliyun.definition.service;

import com.aliyun.oss.OSS;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.core.service.BaseOssService;

/**
 * <p>Aliyun OSS 基础 Service 抽象定义 </p>
 *
 * @author goya
 * @since 2023/7/23 11:56
 */
public abstract class BaseAliyunService extends BaseOssService<OSS> {

    protected BaseAliyunService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }
}
