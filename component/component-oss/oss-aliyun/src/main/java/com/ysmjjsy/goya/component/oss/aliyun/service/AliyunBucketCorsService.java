package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CORSConfiguration;
import com.aliyun.oss.model.GenericRequest;
import com.aliyun.oss.model.SetBucketCORSRequest;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Aliyun OSS 存储桶跨域 Service </p>
 *
 * @author goya
 * @since 2023/7/23 18:10
 */
@Slf4j
@Service
public class AliyunBucketCorsService extends BaseAliyunService {

    public AliyunBucketCorsService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public VoidResult setBucketCORS(SetBucketCORSRequest request) {
        String function = "setBucketCORS";

        OSS client = getClient();

        try {
            return client.setBucketCORS(request);
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

    public CORSConfiguration getBucketCORS(GenericRequest request) {
        String function = "getBucketCORS";

        OSS client = getClient();

        try {
            return client.getBucketCORS(request);
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

    public List<SetBucketCORSRequest.CORSRule> getBucketCORSRules(GenericRequest request) {
        String function = "getBucketCORSRules";

        OSS client = getClient();

        try {
            return client.getBucketCORSRules(request);
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

    public VoidResult deleteBucketCORSRules(GenericRequest request) {
        String function = "deleteBucketCORSRules";

        OSS client = getClient();

        try {
            return client.deleteBucketCORSRules(request);
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
