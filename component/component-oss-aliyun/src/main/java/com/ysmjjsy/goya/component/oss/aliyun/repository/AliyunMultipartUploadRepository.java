package com.ysmjjsy.goya.component.oss.aliyun.repository;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.framework.oss.arguments.multipart.*;
import com.ysmjjsy.goya.component.framework.oss.core.repository.OssMultipartUploadRepository;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.*;
import com.ysmjjsy.goya.component.oss.aliyun.converter.arguments.*;
import com.ysmjjsy.goya.component.oss.aliyun.converter.domain.*;
import com.ysmjjsy.goya.component.oss.aliyun.definition.service.BaseAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

/**
 * <p>Aliyun 兼容模式分片上传操作处理适配器 </p>
 *
 * @author goya
 * @since 2023/8/13 21:13
 */
@Slf4j
@Service
public class AliyunMultipartUploadRepository extends BaseAliyunService implements OssMultipartUploadRepository {

    public AliyunMultipartUploadRepository(AbstractObjectPool<OSS> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    @Override
    public InitiateMultipartUploadDomain initiateMultipartUpload(InitiateMultipartUploadArguments arguments) {
        String function = "initiateMultipartUpload";

        Converter<InitiateMultipartUploadArguments, InitiateMultipartUploadRequest> toRequest = new ArgumentsToInitiateMultipartUploadRequestConverter();
        Converter<InitiateMultipartUploadResult, InitiateMultipartUploadDomain> toDomain = new InitiateMultipartUploadResultToDomainConverter();

        OSS client = getClient();

        try {
            InitiateMultipartUploadResult result = client.initiateMultipartUpload(toRequest.convert(arguments));
            return toDomain.convert(result);
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
    public UploadPartDomain uploadPart(UploadPartArguments arguments) {
        String function = "uploadPart";

        Converter<UploadPartArguments, UploadPartRequest> toRequest = new ArgumentsToUploadPartRequestConverter();
        Converter<UploadPartResult, UploadPartDomain> toDomain = new UploadPartResultToDomainConverter();

        OSS client = getClient();

        try {
            UploadPartResult result = client.uploadPart(toRequest.convert(arguments));
            return toDomain.convert(result);
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
    public UploadPartCopyDomain uploadPartCopy(UploadPartCopyArguments arguments) {
        String function = "uploadPartCopy";

        Converter<UploadPartCopyArguments, UploadPartCopyRequest> toRequest = new ArgumentsToUploadPartCopyRequestConverter();
        Converter<UploadPartCopyResult, UploadPartCopyDomain> toDomain = new UploadPartCopyResultToDomainConverter();

        OSS client = getClient();

        try {
            UploadPartCopyResult result = client.uploadPartCopy(toRequest.convert(arguments));
            return toDomain.convert(result);
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
    public CompleteMultipartUploadDomain completeMultipartUpload(CompleteMultipartUploadArguments arguments) {
        String function = "completeMultipartUpload";

        Converter<CompleteMultipartUploadArguments, CompleteMultipartUploadRequest> toRequest = new ArgumentsToCompleteMultipartUploadRequestConverter();
        Converter<CompleteMultipartUploadResult, CompleteMultipartUploadDomain> toDomain = new CompleteMultipartUploadResultToDomainConverter();

        OSS client = getClient();

        try {
            CompleteMultipartUploadResult result = client.completeMultipartUpload(toRequest.convert(arguments));
            return toDomain.convert(result);
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
    public AbortMultipartUploadDomain abortMultipartUpload(AbortMultipartUploadArguments arguments) {
        String function = "abortMultipartUpload";

        Converter<AbortMultipartUploadArguments, AbortMultipartUploadRequest> toRequest = new ArgumentsToAbortMultipartUploadRequestConverter();

        OSS client = getClient();

        try {
            client.abortMultipartUpload(toRequest.convert(arguments));
            AbortMultipartUploadDomain domain = new AbortMultipartUploadDomain();
            domain.setUploadId(arguments.getUploadId());
            domain.setBucketName(arguments.getBucketName());
            domain.setObjectName(arguments.getObjectName());
            return domain;
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
    public ListPartsDomain listParts(ListPartsArguments arguments) {
        String function = "listParts";

        Converter<ListPartsArguments, ListPartsRequest> toRequest = new ArgumentsToListPartsRequestConverter();
        Converter<PartListing, ListPartsDomain> toDomain = new PartListingToDomainConverter();

        OSS client = getClient();

        try {
            PartListing listing = client.listParts(toRequest.convert(arguments));
            return toDomain.convert(listing);
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
    public ListMultipartUploadsDomain listMultipartUploads(ListMultipartUploadsArguments arguments) {
        String function = "listMultipartUploads";

        Converter<ListMultipartUploadsArguments, ListMultipartUploadsRequest> toRequest = new ArgumentsToListMultipartUploadsRequest();
        Converter<MultipartUploadListing, ListMultipartUploadsDomain> toDomain = new MultipartUploadListingToDomainConverter();

        OSS client = getClient();

        try {
            MultipartUploadListing listing = client.listMultipartUploads(toRequest.convert(arguments));
            return toDomain.convert(listing);
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
