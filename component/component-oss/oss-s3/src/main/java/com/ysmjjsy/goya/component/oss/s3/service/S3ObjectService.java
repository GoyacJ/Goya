package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Amazon S3 对象管理 Service </p>
 *
 * @author goya
 * @since 2023/7/16 16:48
 */
@Slf4j
@Service
public class S3ObjectService extends BaseS3Service {

    public S3ObjectService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 获取对象详细信息
     *
     */

    /**
     * 上传对象
     *
     */


    /**
     * 复制对象
     *
     */


    /**
     * 删除对象指定版本
     *
     */


    /**
     * 删除对象指定版本
     *
     */


    /**
     * 删除对象指定版本
     *
     */


    /**
     * 删除对象指定版本
     *
     */


    /**
     * 列出下一批对象
     *
     */



}
