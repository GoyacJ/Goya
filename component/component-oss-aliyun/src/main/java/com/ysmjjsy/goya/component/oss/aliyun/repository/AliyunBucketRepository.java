package com.ysmjjsy.goya.component.oss.aliyun.repository;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.GenericRequest;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.framework.oss.arguments.bucket.CreateBucketArguments;
import com.ysmjjsy.goya.component.framework.oss.arguments.bucket.DeleteBucketArguments;
import com.ysmjjsy.goya.component.framework.oss.core.repository.OssBucketRepository;
import com.ysmjjsy.goya.component.framework.oss.domain.bucket.BucketDomain;
import com.ysmjjsy.goya.component.framework.oss.utils.OssConverterUtils;
import com.ysmjjsy.goya.component.oss.aliyun.converter.arguments.ArgumentsToCreateBucketRequestConverter;
import com.ysmjjsy.goya.component.oss.aliyun.converter.arguments.ArgumentsToDeleteBucketRequestConverter;
import com.ysmjjsy.goya.component.oss.aliyun.converter.domain.BucketToDomainConverter;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Aliyun 兼容模式存储桶操作处理器 </p>
 *
 * @author goya
 * @since 2023/7/24 19:15
 */
@Slf4j
@Service
public class AliyunBucketRepository extends BaseAliyunService implements OssBucketRepository {

    public AliyunBucketRepository(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    @Override
    public boolean doesBucketExist(String bucketName) {
        String function = "doesBucketExist";

        OSS client = getClient();

        try {
            return client.doesBucketExist(bucketName);
        } catch (ClientException e) {
            log.error("[Goya] |- Catch ClientException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } catch (OSSException e) {
            log.error("[Goya] |- Catch OSSException in [{}].", function, e);
            throw new GoyaException(e.getMessage());
        } finally {
            close(client);
        }
    }

    @Override
    public List<BucketDomain> listBuckets() {
        String function = "listBuckets";

        OSS client = getClient();

        try {
            return OssConverterUtils.toDomains(client.listBuckets(), new BucketToDomainConverter());
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

    @Override
    public BucketDomain createBucket(String bucketName) {
        String function = "createBucket";

        OSS client = getClient();

        try {
            return OssConverterUtils.toDomain(client.createBucket(bucketName), new BucketToDomainConverter());
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

    @Override
    public BucketDomain createBucket(CreateBucketArguments arguments) {
        String function = "createBucket";

        OSS client = getClient();

        try {
            Converter<CreateBucketArguments, CreateBucketRequest> toRequest = new ArgumentsToCreateBucketRequestConverter();
            return OssConverterUtils.toDomain(client.createBucket(toRequest.convert(arguments)), new BucketToDomainConverter());
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

    @Override
    public void deleteBucket(String bucketName) {
        String function = "deleteBucket";

        OSS client = getClient();

        try {
            client.deleteBucket(bucketName);
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

    @Override
    public void deleteBucket(DeleteBucketArguments arguments) {
        String function = "deleteBucket";

        OSS client = getClient();

        try {
            Converter<DeleteBucketArguments, GenericRequest> toRequest = new ArgumentsToDeleteBucketRequestConverter();
            client.deleteBucket(toRequest.convert(arguments));
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
