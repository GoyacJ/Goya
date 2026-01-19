package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Amazon S3 存储桶生命度量配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:16
 */
@Slf4j
@Service
public class S3BucketMetricsConfigurationService extends BaseS3Service {

    public S3BucketMetricsConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶生命度量配置
     *
     */

    /**
     * 获取存储桶度量配置
     *
     */

    /**
     * 获取存储桶度量配置列表
     *
     */

    /**
     * 设置存储桶度量配置列表
     *
     */
}
