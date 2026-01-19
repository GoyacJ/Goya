package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Amazon S3 存储桶库存配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:11
 */
@Slf4j
@Service
public class S3BucketInventoryConfiguration extends BaseS3Service {

    public S3BucketInventoryConfiguration(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶库存配置
     *
     */

    /**
     * 获取存储桶库存配置
     *
     */

    /**
     * 获取存储桶库存配置列表
     *
     */

    /**
     * 设置存储桶库存配置列表
     *
     */
}
