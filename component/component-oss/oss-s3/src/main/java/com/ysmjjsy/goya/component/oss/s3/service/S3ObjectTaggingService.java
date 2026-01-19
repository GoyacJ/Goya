package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Amazon S3 对象标记 Service </p>
 *
 * @author goya
 * @since 2023/7/16 18:47
 */
@Slf4j
@Service
public class S3ObjectTaggingService extends BaseS3Service {

    public S3ObjectTaggingService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除对象标记
     *
     */


    /**
     * 获取对象标记设置
     *
     */


    /**
     * 设置对象标记设置
     *
     */

}
