package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Amazon S3 对象访问控制列表 Service </p>
 *
 * @author goya
 * @since 2023/7/16 21:12
 */
@Slf4j
@Service
public class S3ObjectAccessControlListService extends BaseS3Service {

    public S3ObjectAccessControlListService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取对象访问控制列表
     *
     */


    /**
     * 设置对象访问控制列表
     *
     */

}
