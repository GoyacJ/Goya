package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Amazon S3 公共访问块 Service </p>
 *
 * @author goya
 * @since 2023/7/16 18:49
 */
@Slf4j
@Service
public class S3PublicAccessBlockService extends BaseS3Service {

    public S3PublicAccessBlockService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 删除公共访问块
     *
     */

    /**
     * 获取公共访问块
     *
     */


    /**
     * 设置对象标记设置
     *
     */

}
