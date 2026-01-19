package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Amazon S3 存储桶加速配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 18:52
 */
@Slf4j
@Service
public class S3BucketAccelerateConfigurationService extends BaseS3Service {

    public S3BucketAccelerateConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取存储桶加速配置
     *
     */


    /**
     * 设置存储桶加速配置
     *
     */

}
