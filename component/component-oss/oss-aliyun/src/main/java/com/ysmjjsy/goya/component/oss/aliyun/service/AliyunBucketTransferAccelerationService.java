package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.TransferAcceleration;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 传输加速 Service </p>
 *
 * @author goya
 * @since 2023/7/23 22:55
 */
@Slf4j
@Service
public class AliyunBucketTransferAccelerationService extends BaseAliyunService {

    public AliyunBucketTransferAccelerationService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    /**
     * 设置存储桶传输加速
     *
     * @param bucketName 存储桶名称
     * @param enable     状态
     * @return {@link VoidResult}
     */
    public VoidResult setBucketTransferAcceleration(String bucketName, boolean enable) {
        String function = "setBucketTransferAcceleration";

        OSS client = getClient();

        try {
            return client.setBucketTransferAcceleration(bucketName, enable);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(client);
        }
    }

    /**
     * 获取存储桶加速传输
     *
     * @param bucketName 存储桶名称
     * @return {@link TransferAcceleration}
     */
    public TransferAcceleration getBucketTransferAcceleration(String bucketName) {
        String function = "getBucketTransferAcceleration";

        OSS client = getClient();

        try {
            return client.getBucketTransferAcceleration(bucketName);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(client);
        }
    }

    /**
     * 删除存储桶加速传输
     *
     * @param bucketName 存储桶名称
     * @return {@link VoidResult}
     */
    public VoidResult deleteBucketTransferAcceleration(String bucketName) {
        String function = "deleteBucketTransferAcceleration";

        OSS client = getClient();

        try {
            return client.deleteBucketTransferAcceleration(bucketName);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new CommonException(e.getMessage());
        } finally {
            close(client);
        }
    }
}
