package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.BucketMetadata;
import com.aliyun.oss.model.GenericRequest;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.oss.aliyun.definition.pool.AliyunClientObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 存储桶 Service </p>
 *
 * @author goya
 * @since 2023/7/23 11:58
 */
@Slf4j
@Service
public class AliyunBucketService extends BaseAliyunService {

    protected AliyunBucketService(AliyunClientObjectPool aliyunClientObjectPool) {
        super(aliyunClientObjectPool);
    }

    public BucketMetadata getBucketMetadata(GenericRequest request) {
        String function = "getBucketMetadata";

        OSS client = getClient();

        try {
            return client.getBucketMetadata(request);
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


    public String getBucketLocation(GenericRequest request) {
        String function = "getBucketLocation";

        OSS client = getClient();

        try {
            return client.getBucketLocation(request);
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
