package com.ysmjjsy.goya.component.oss.aliyun.repository;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.core.arguments.object.*;
import com.ysmjjsy.goya.component.oss.core.core.repository.OssObjectRepository;
import com.ysmjjsy.goya.component.oss.core.domain.base.ObjectWriteDomain;
import com.ysmjjsy.goya.component.oss.core.domain.object.*;
import com.ysmjjsy.goya.component.oss.aliyun.converter.arguments.ArgumentsToDeleteObjectRequestConverter;
import com.ysmjjsy.goya.component.oss.aliyun.converter.arguments.ArgumentsToDeleteObjectsRequestConverter;
import com.ysmjjsy.goya.component.oss.aliyun.converter.arguments.ArgumentsToListObjectsRequestConverter;
import com.ysmjjsy.goya.component.oss.aliyun.converter.arguments.ArgumentsToListObjectsV2RequestConverter;
import com.ysmjjsy.goya.component.oss.aliyun.converter.domain.DeleteObjectsResultToDomainConverter;
import com.ysmjjsy.goya.component.oss.aliyun.converter.domain.ListObjectsV2ResultToDomainConverter;
import com.ysmjjsy.goya.component.oss.aliyun.converter.domain.ObjectListingToDomainConverter;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Aliyun 兼容模式对象操作处理器 </p>
 *
 * @author goya
 * @since 2023/8/9 16:49
 */
@Slf4j
@Service
public class AliyunObjectRepository extends BaseAliyunService implements OssObjectRepository {

    public AliyunObjectRepository(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    @Override
    public ListObjectsDomain listObjects(ListObjectsArguments arguments) {
        String function = "listObjects";

        Converter<ListObjectsArguments, ListObjectsRequest> toArgs = new ArgumentsToListObjectsRequestConverter();
        Converter<ObjectListing, ListObjectsDomain> toDomain = new ObjectListingToDomainConverter();

        OSS client = getClient();

        try {
            ObjectListing objectListing = client.listObjects(toArgs.convert(arguments));
            return toDomain.convert(objectListing);
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

    @Override
    public ListObjectsV2Domain listObjectsV2(ListObjectsV2Arguments arguments) {
        String function = "listObjectsV2";

        Converter<ListObjectsV2Arguments, ListObjectsV2Request> toArgs = new ArgumentsToListObjectsV2RequestConverter();
        Converter<ListObjectsV2Result, ListObjectsV2Domain> toDomain = new ListObjectsV2ResultToDomainConverter();

        OSS client = getClient();

        try {
            ListObjectsV2Result listObjectsV2Result = client.listObjectsV2(toArgs.convert(arguments));
            return toDomain.convert(listObjectsV2Result);
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

    @Override
    public void deleteObject(DeleteObjectArguments arguments) {
        String function = "deleteObject";

        OSS client = getClient();

        try {
            Converter<DeleteObjectArguments, GenericRequest> toArgs = new ArgumentsToDeleteObjectRequestConverter();
            client.deleteObject(toArgs.convert(arguments));
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

    @Override
    public List<DeleteObjectDomain> deleteObjects(DeleteObjectsArguments arguments) {

        String function = "deleteObjects";

        OSS client = getClient();

        try {
            Converter<DeleteObjectsArguments, DeleteObjectsRequest> toArgs = new ArgumentsToDeleteObjectsRequestConverter();
            Converter<DeleteObjectsResult, List<DeleteObjectDomain>> toDomain = new DeleteObjectsResultToDomainConverter();

            DeleteObjectsResult result = client.deleteObjects(toArgs.convert(arguments));
            return toDomain.convert(result);
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

    @Override
    public ObjectMetadataDomain getObjectMetadata(GetObjectMetadataArguments arguments) {
        return null;
    }

    @Override
    public GetObjectDomain getObject(GetObjectArguments arguments) {
        return null;
    }

    @Override
    public PutObjectDomain putObject(PutObjectArguments arguments) {
        return null;
    }

    @Override
    public String generatePresignedUrl(GeneratePresignedUrlArguments arguments) {
        return null;
    }

    @Override
    public ObjectMetadataDomain download(DownloadObjectArguments arguments) {
        return null;
    }

    @Override
    public ObjectWriteDomain upload(UploadObjectArguments arguments) {
        return null;
    }
}
