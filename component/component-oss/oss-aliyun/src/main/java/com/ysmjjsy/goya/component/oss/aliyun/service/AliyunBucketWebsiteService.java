package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.BucketWebsiteResult;
import com.aliyun.oss.model.GenericRequest;
import com.aliyun.oss.model.SetBucketWebsiteRequest;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 存储桶网址 Service </p>
 *
 * @author goya
 * @since 2023/7/23 18:16
 */
@Slf4j
@Service
public class AliyunBucketWebsiteService extends BaseAliyunService {

    public AliyunBucketWebsiteService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public VoidResult setBucketWebsite(SetBucketWebsiteRequest request) {
        String function = "setBucketWebsite";

        OSS client = getClient();

        try {
            return client.setBucketWebsite(request);
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

    public BucketWebsiteResult getBucketWebsite(GenericRequest request) {
        String function = "getBucketWebsite";

        OSS client = getClient();

        try {
            return client.getBucketWebsite(request);
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

    public VoidResult deleteBucketWebsite(GenericRequest request) {
        String function = "deleteBucketWebsite";

        OSS client = getClient();

        try {
            return client.deleteBucketWebsite(request);
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
