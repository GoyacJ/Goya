package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GenericRequest;
import com.aliyun.oss.model.LifecycleRule;
import com.aliyun.oss.model.SetBucketLifecycleRequest;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Aliyun OSS 存储桶生命周期 Service </p>
 *
 * @author goya
 * @since 2023/7/23 18:20
 */
@Slf4j
@Service
public class AliyunBucketLifecycleService extends BaseAliyunService {

    public AliyunBucketLifecycleService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }


    public VoidResult setBucketLifecycle(SetBucketLifecycleRequest request) {
        String function = "setBucketLifecycle";

        OSS client = getClient();

        try {
            return client.setBucketLifecycle(request);
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

    public List<LifecycleRule> getBucketLifecycle(GenericRequest request) {
        String function = "getBucketLifecycle";

        OSS client = getClient();

        try {
            return client.getBucketLifecycle(request);
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

    public VoidResult deleteBucketLifecycle(GenericRequest request) {
        String function = "deleteBucketLifecycle";

        OSS client = getClient();

        try {
            return client.deleteBucketLifecycle(request);
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
