package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.BucketQosInfo;
import com.aliyun.oss.model.GenericRequest;
import com.aliyun.oss.model.SetBucketQosInfoRequest;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 存储桶Qos Service </p>
 *
 * @author goya
 * @since 2023/7/23 21:47
 */
@Slf4j
@Service
public class AliyunBucketQosInfoService extends BaseAliyunService {

    public AliyunBucketQosInfoService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public VoidResult setBucketQosInfo(SetBucketQosInfoRequest request) {
        String function = "setBucketQosInfo";

        OSS client = getClient();

        try {
            return client.setBucketQosInfo(request);
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

    public BucketQosInfo getBucketQosInfo(GenericRequest request) {
        String function = "getBucketQosInfo";

        OSS client = getClient();

        try {
            return client.getBucketQosInfo(request);
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

    public VoidResult deleteBucketQosInfo(GenericRequest request) {
        String function = "deleteBucketQosInfo";

        OSS client = getClient();

        try {
            return client.deleteBucketQosInfo(request);
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
