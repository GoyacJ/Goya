package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GenericRequest;
import com.aliyun.oss.model.GetBucketImageResult;
import com.aliyun.oss.model.PutBucketImageRequest;
import com.aliyun.oss.model.VoidResult;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun OSS 存储桶镜像 Service </p>
 *
 * @author goya
 * @since 2023/7/23 16:47
 */
@Slf4j
@Service
public class AliyunBucketImageService extends BaseAliyunService {

    public AliyunBucketImageService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public VoidResult putBucketImage(PutBucketImageRequest request) {
        String function = "putBucketImage";

        OSS client = getClient();

        try {
            return client.putBucketImage(request);
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

    public GetBucketImageResult getBucketImage(String bucketName, GenericRequest request) {
        String function = "getBucketImage";

        OSS client = getClient();

        try {
            return client.getBucketImage(bucketName, request);
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

    public VoidResult deleteBucketImage(String bucketName, GenericRequest request) {
        String function = "deleteBucketImage";

        OSS client = getClient();

        try {
            return client.deleteBucketImage(bucketName, request);
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
