package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Amazon S3 存储桶管理 Service </p>
 *
 * @author goya
 * @since 2023/7/14 16:04
 */
@Slf4j
@Service
public class S3BucketService extends BaseS3Service {

    public S3BucketService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

    /**
     * 返回指定存储桶中版本的摘要信息列表
     *
     */


    /**
     * 此操作可用于确定存储桶是否存在以及您是否有权访问它。如果存储桶存在并且您有权访问，则此操作返回200 OK。
     *
     */


    /**
     * 获取存储桶位置
     *
     */

}
