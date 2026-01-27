package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Aliyun OSS 存储桶复制 Service </p>
 *
 * @author goya
 * @since 2023/7/23 18:20
 */
@Slf4j
@Service
public class AliyunBucketReplicationService extends BaseAliyunService {

    public AliyunBucketReplicationService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public VoidResult addBucketReplication(AddBucketReplicationRequest request) {
        String function = "addBucketReplication";

        OSS client = getClient();

        try {
            return client.addBucketReplication(request);
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

    public List<ReplicationRule> getBucketReplication(GenericRequest request) {
        String function = "getBucketReplication";

        OSS client = getClient();

        try {
            return client.getBucketReplication(request);
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

    public VoidResult deleteBucketReplication(DeleteBucketReplicationRequest request) {
        String function = "deleteBucketReplication";

        OSS client = getClient();

        try {
            return client.deleteBucketReplication(request);
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

    public BucketReplicationProgress getBucketReplicationProgress(GetBucketReplicationProgressRequest request) {
        String function = "getBucketReplicationProgress";

        OSS client = getClient();

        try {
            return client.getBucketReplicationProgress(request);
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

    public List<String> getBucketReplicationLocation(GenericRequest request) {
        String function = "getBucketReplicationLocation";

        OSS client = getClient();

        try {
            return client.getBucketReplicationLocation(request);
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
