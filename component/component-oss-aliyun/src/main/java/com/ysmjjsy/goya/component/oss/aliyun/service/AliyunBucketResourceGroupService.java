package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GetBucketResourceGroupResult;
import com.aliyun.oss.model.SetBucketResourceGroupRequest;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 存储桶资源组 Service </p>
 *
 * @author goya
 * @since 2023/7/23 22:20
 */
@Slf4j
@Service
public class AliyunBucketResourceGroupService extends BaseAliyunService {

    public AliyunBucketResourceGroupService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public VoidResult setBucketResourceGroup(SetBucketResourceGroupRequest request) {
        String function = "setBucketResourceGroup";

        OSS client = getClient();

        try {
            return client.setBucketResourceGroup(request);
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

    public GetBucketResourceGroupResult getBucketResourceGroup(String bucketName) {
        String function = "getBucketResourceGroup";

        OSS client = getClient();

        try {
            return client.getBucketResourceGroup(bucketName);
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
