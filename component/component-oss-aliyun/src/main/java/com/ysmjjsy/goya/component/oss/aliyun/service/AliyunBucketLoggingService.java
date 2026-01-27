package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.BucketLoggingResult;
import com.aliyun.oss.model.GenericRequest;
import com.aliyun.oss.model.SetBucketLoggingRequest;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 存储桶日志记录 Service </p>
 *
 * @author goya
 * @since 2023/7/23 18:14
 */
@Slf4j
@Service
public class AliyunBucketLoggingService extends BaseAliyunService {

    public AliyunBucketLoggingService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public VoidResult setBucketLogging(SetBucketLoggingRequest request) {
        String function = "setBucketLogging";

        OSS client = getClient();

        try {
            return client.setBucketLogging(request);
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

    public BucketLoggingResult getBucketLogging(GenericRequest request) {
        String function = "getBucketLogging";

        OSS client = getClient();

        try {
            return client.getBucketLogging(request);
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

    public VoidResult deleteBucketLogging(GenericRequest request) {
        String function = "deleteBucketLogging";

        OSS client = getClient();

        try {
            return client.deleteBucketLogging(request);
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
