package com.ysmjjsy.goya.component.oss.s3.definition.service;

import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.framework.oss.service.BaseOssService;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * <p>Amazon S3 基础服务 </p>
 *
 * @author goya
 * @since 2023/7/14 16:57
 */
public abstract class BaseS3Service extends BaseOssService<S3Client> {

    protected BaseS3Service(AbstractObjectPool<S3Client> ossClientObjectPool) {
        super(ossClientObjectPool);
    }
}
