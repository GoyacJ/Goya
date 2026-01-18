package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GenericRequest;
import com.aliyun.oss.model.GetBucketRequestPaymentResult;
import com.aliyun.oss.model.SetBucketRequestPaymentRequest;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 存储桶请求付费 Service </p>
 *
 * @author goya
 * @since 2023/7/23 21:45
 */
@Slf4j
@Service
public class AliyunBucketRequestPaymentService extends BaseAliyunService {

    public AliyunBucketRequestPaymentService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public VoidResult setBucketRequestPayment(SetBucketRequestPaymentRequest request) {
        String function = "setBucketRequestPayment";

        OSS client = getClient();

        try {
            return client.setBucketRequestPayment(request);
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

    public GetBucketRequestPaymentResult getBucketRequestPayment(GenericRequest request) {
        String function = "getBucketRequestPayment";

        OSS client = getClient();

        try {
            return client.getBucketRequestPayment(request);
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
