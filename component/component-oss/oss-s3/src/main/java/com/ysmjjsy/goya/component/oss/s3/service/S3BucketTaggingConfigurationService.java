package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Amazon S3 存储桶标记配置 Service </p>
 *
 * @author goya
 * @since 2023/7/16 17:28
 */
@Slf4j
@Service
public class S3BucketTaggingConfigurationService extends BaseS3Service {

    public S3BucketTaggingConfigurationService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除存储桶标记配置
     *
     */


    /**
     * 获取存储标记配置
     *
     */


    /**
     * 设置存储标记配置
     *
     */

}
