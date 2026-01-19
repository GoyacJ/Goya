package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Presigned Url Service </p>
 *
 * @author goya
 * @since 2023/7/16 22:30
 */
@Slf4j
@Service
public class S3PresignedUrlService extends BaseS3Service {

    public S3PresignedUrlService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取对象标记设置
     *
     */


    /**
     * Presigned Url download
     */

    /**
     * Presigned Url upload
     */
}
