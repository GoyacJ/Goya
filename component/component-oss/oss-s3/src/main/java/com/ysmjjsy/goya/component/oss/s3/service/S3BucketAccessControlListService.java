package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Amazon S3 存储桶访问控制列表 Service </p>
 *
 * @author goya
 * @since 2023/7/16 18:55
 */
@Slf4j
@Service
public class S3BucketAccessControlListService extends BaseS3Service {

    public S3BucketAccessControlListService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取存储桶访问控制列表
     *
     */

    /**
     * 设置存储桶访问控制列表
     *
     */
}
