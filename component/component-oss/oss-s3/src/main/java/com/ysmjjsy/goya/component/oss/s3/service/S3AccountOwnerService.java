package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>获取请求的已验证发件人正在使用的Amazon Web Services帐户的当前所有者。 </p>
 *
 * @author goya
 * @since 2023/7/16 22:16
 */
@Slf4j
@Service
public class S3AccountOwnerService extends BaseS3Service {

    public S3AccountOwnerService(S3ClientObjectPool s3ClientObjectPool) {
        super(s3ClientObjectPool);
    }

}
