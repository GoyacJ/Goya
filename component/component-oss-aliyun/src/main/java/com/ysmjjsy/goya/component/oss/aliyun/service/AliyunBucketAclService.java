package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.AccessControlList;
import com.aliyun.oss.model.GenericRequest;
import com.aliyun.oss.model.SetBucketAclRequest;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 存储桶访问控制列表 Service </p>
 *
 * @author goya
 * @since 2023/7/23 16:19
 */
@Slf4j
@Service
public class AliyunBucketAclService extends BaseAliyunService {

    public AliyunBucketAclService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public VoidResult setBucketAcl(SetBucketAclRequest request) {
        String function = "setBucketAcl";

        OSS client = getClient();

        try {
            return client.setBucketAcl(request);
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

    public AccessControlList getBucketAcl(GenericRequest request) {
        String function = "getBucketAcl";

        OSS client = getClient();

        try {
            return client.getBucketAcl(request);
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
