package com.ysmjjsy.goya.component.oss.aliyun.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Aliyun OSS 存储桶 Cname Service </p>
 *
 * @author goya
 * @since 2023/7/23 18:21
 */
@Slf4j
@Service
public class AliyunBucketCnameService extends BaseAliyunService {

    public AliyunBucketCnameService(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    public AddBucketCnameResult addBucketCname(AddBucketCnameRequest request) {
        String function = "addBucketCname";

        OSS client = getClient();

        try {
            return client.addBucketCname(request);
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

    public List<CnameConfiguration> getBucketCname(GenericRequest request) {
        String function = "getBucketCname";

        OSS client = getClient();

        try {
            return client.getBucketCname(request);
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

    public VoidResult deleteBucketCname(DeleteBucketCnameRequest request) {
        String function = "deleteBucketCname";

        OSS client = getClient();

        try {
            return client.deleteBucketCname(request);
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

    public CreateBucketCnameTokenResult createBucketCnameToken(CreateBucketCnameTokenRequest request) {
        String function = "createBucketCnameToken";

        OSS client = getClient();

        try {
            return client.createBucketCnameToken(request);
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

    public GetBucketCnameTokenResult getBucketCnameToken(GetBucketCnameTokenRequest request) {
        String function = "getBucketCnameToken";

        OSS client = getClient();

        try {
            return client.getBucketCnameToken(request);
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

    public BucketInfo getBucketInfo(GenericRequest request) {
        String function = "getBucketInfo";

        OSS client = getClient();

        try {
            return client.getBucketInfo(request);
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

    public BucketStat getBucketStat(GenericRequest request) {
        String function = "getBucketStat";

        OSS client = getClient();

        try {
            return client.getBucketStat(request);
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
