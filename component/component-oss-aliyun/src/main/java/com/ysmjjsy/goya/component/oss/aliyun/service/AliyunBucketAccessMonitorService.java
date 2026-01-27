package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.AccessMonitor;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 存储桶访问监控 Service </p>
 *
 * @author goya
 * @since 2023/7/23 22:08
 */
@Slf4j
@Service
public class AliyunBucketAccessMonitorService extends BaseAliyunService {

    public AliyunBucketAccessMonitorService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    /**
     * 设置 BucketAccessMonitor
     *
     * @param bucketName 存储桶名称
     * @param status     存储桶访问监控状态
     * @return {@link VoidResult}
     */
    public VoidResult putBucketAccessMonitor(String bucketName, String status) {
        String function = "putBucketAccessMonitor";

        OSS client = getClient();

        try {
            return client.putBucketAccessMonitor(bucketName, status);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(client);
        }
    }

    /**
     * 获取 BucketAccessMonitor
     *
     * @param bucketName 存储桶名称
     * @return {@link AccessMonitor}
     */
    public AccessMonitor getBucketAccessMonitor(String bucketName) {
        String function = "getBucketAccessMonitor";

        OSS client = getClient();

        try {
            return client.getBucketAccessMonitor(bucketName);
        } catch (ClientException e) {
            log.error("[Goya] |- Aliyun OSS catch ClientException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Aliyun OSS catch OSSException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(client);
        }
    }
}
