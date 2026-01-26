package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GenericRequest;
import com.aliyun.oss.model.ServerSideEncryptionConfiguration;
import com.aliyun.oss.model.SetBucketEncryptionRequest;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 存储桶加密 Service </p>
 *
 * @author goya
 * @since 2023/7/23 18:35
 */
@Slf4j
@Service
public class AliyunBucketEncryptionService extends BaseAliyunService {

    public AliyunBucketEncryptionService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public VoidResult setBucketEncryption(SetBucketEncryptionRequest request) {
        String function = "setBucketEncryption";

        OSS client = getClient();

        try {
            return client.setBucketEncryption(request);
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

    public ServerSideEncryptionConfiguration getBucketEncryption(GenericRequest request) {
        String function = "getBucketEncryption";

        OSS client = getClient();

        try {
            return client.getBucketEncryption(request);
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

    public VoidResult deleteBucketEncryption(GenericRequest request) {
        String function = "deleteBucketEncryption";

        OSS client = getClient();

        try {
            return client.deleteBucketEncryption(request);
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
