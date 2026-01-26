package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GenericRequest;
import com.aliyun.oss.model.GetBucketCallbackPolicyResult;
import com.aliyun.oss.model.SetBucketCallbackPolicyRequest;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 存储桶回调策略 Service </p>
 *
 * @author goya
 * @since 2023/7/23 22:03
 */
@Slf4j
@Service
public class AliyunBucketCallbackPolicyService extends BaseAliyunService {

    public AliyunBucketCallbackPolicyService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    /**
     * 设置存储桶实例的回调策略
     *
     * @param request {@link SetBucketCallbackPolicyRequest}
     * @return {@link VoidResult}
     */
    public VoidResult setBucketCallbackPolicy(SetBucketCallbackPolicyRequest request) {
        String function = "setBucketCallbackPolicy";

        OSS client = getClient();

        try {
            return client.setBucketCallbackPolicy(request);
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
     * 获取存储桶实例的回调策略
     *
     * @param request {@link GenericRequest}
     * @return {@link GetBucketCallbackPolicyResult}
     */
    public GetBucketCallbackPolicyResult getBucketCallbackPolicy(GenericRequest request) {
        String function = "getBucketCallbackPolicy";

        OSS client = getClient();

        try {
            return client.getBucketCallbackPolicy(request);
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
     * 删除存储桶实例的回调策略
     *
     * @param request {@link GenericRequest}
     * @return {@link VoidResult}
     */
    public VoidResult deleteBucketCallbackPolicy(GenericRequest request) {
        String function = "deleteBucketCallbackPolicy";

        OSS client = getClient();

        try {
            return client.deleteBucketCallbackPolicy(request);
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
