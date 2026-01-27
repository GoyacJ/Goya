package com.ysmjjsy.goya.component.oss.s3.repository;

import com.ysmjjsy.goya.component.framework.common.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.framework.oss.arguments.multipart.*;
import com.ysmjjsy.goya.component.framework.oss.core.repository.OssMultipartUploadRepository;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.*;
import com.ysmjjsy.goya.component.oss.s3.converter.arguments.*;
import com.ysmjjsy.goya.component.oss.s3.converter.domain.*;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * <p>Amazon S3 Java OSS API 分片上传操作实现</p>
 *
 * @author goya
 * @since 2023/8/13 21:12
 */
@Slf4j
@Service
public class S3MultipartUploadRepository extends BaseS3Service implements OssMultipartUploadRepository {

    public S3MultipartUploadRepository(AbstractObjectPool<S3Client> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    @Override
    public InitiateMultipartUploadDomain initiateMultipartUpload(InitiateMultipartUploadArguments arguments) {
        String function = "initiateMultipartUpload";
        S3Client client = getClient();
        try {
            Converter<InitiateMultipartUploadArguments, CreateMultipartUploadRequest> toRequest =
                    new ArgumentsToInitiateMultipartUploadRequestConverter();
            Converter<CreateMultipartUploadResponse, InitiateMultipartUploadDomain> toDomain =
                    new CreateMultipartUploadResponseToDomainConverter();

            CreateMultipartUploadRequest request = toRequest.convert(arguments);
            CreateMultipartUploadResponse response = client.createMultipartUpload(request);
            return toDomain.convert(response);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public UploadPartDomain uploadPart(UploadPartArguments arguments) {
        String function = "uploadPart";
        S3Client client = getClient();
        try {
            Converter<UploadPartArguments, UploadPartRequest> toRequest =
                    new ArgumentsToUploadPartRequestConverter();
            Converter<UploadPartResponse, UploadPartDomain> toDomain =
                    new UploadPartResponseToDomainConverter();

            UploadPartRequest request = toRequest.convert(arguments);
            RequestBody requestBody = RequestBody.fromInputStream(
                    arguments.getInputStream(),
                    arguments.getPartSize());

            UploadPartResponse response = client.uploadPart(request, requestBody);
            return toDomain.convert(response);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public UploadPartCopyDomain uploadPartCopy(UploadPartCopyArguments arguments) {
        String function = "uploadPartCopy";
        S3Client client = getClient();
        try {
            Converter<UploadPartCopyArguments, UploadPartCopyRequest> toRequest =
                    new ArgumentsToCopyPartRequestConverter();
            Converter<UploadPartCopyResponse, UploadPartCopyDomain> toDomain =
                    new CopyObjectResponseToDomainConverter();

            UploadPartCopyRequest request = toRequest.convert(arguments);
            UploadPartCopyResponse response = client.uploadPartCopy(request);

            UploadPartCopyDomain domain = toDomain.convert(response);
            domain.setUploadId(arguments.getUploadId());
            domain.setPartNumber(arguments.getPartNumber());

            return domain;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public CompleteMultipartUploadDomain completeMultipartUpload(CompleteMultipartUploadArguments arguments) {
        String function = "completeMultipartUpload";
        S3Client client = getClient();
        try {
            Converter<CompleteMultipartUploadArguments, CompleteMultipartUploadRequest> toRequest =
                    new ArgumentsToCompleteMultipartUploadRequestConverter();
            Converter<CompleteMultipartUploadResponse, CompleteMultipartUploadDomain> toDomain =
                    new CompleteMultipartUploadResponseToDomainConverter();

            CompleteMultipartUploadRequest request = toRequest.convert(arguments);
            CompleteMultipartUploadResponse response = client.completeMultipartUpload(request);
            return toDomain.convert(response);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public AbortMultipartUploadDomain abortMultipartUpload(AbortMultipartUploadArguments arguments) {
        String function = "abortMultipartUpload";
        S3Client client = getClient();
        try {
            Converter<AbortMultipartUploadArguments, AbortMultipartUploadRequest> toRequest =
                    new ArgumentsToAbortMultipartUploadRequestConverter();

            AbortMultipartUploadRequest request = toRequest.convert(arguments);
            client.abortMultipartUpload(request);

            AbortMultipartUploadDomain domain = new AbortMultipartUploadDomain();
            domain.setBucketName(arguments.getBucketName());
            domain.setObjectName(arguments.getObjectName());
            domain.setUploadId(arguments.getUploadId());
            return domain;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public ListPartsDomain listParts(ListPartsArguments arguments) {
        String function = "listParts";
        S3Client client = getClient();
        try {
            Converter<ListPartsArguments, ListPartsRequest> toRequest =
                    new ArgumentsToListPartsRequestConverter();
            Converter<ListPartsResponse, ListPartsDomain> toDomain =
                    new ListPartsResponseToDomainConverter();

            ListPartsRequest request = toRequest.convert(arguments);
            ListPartsResponse response = client.listParts(request);
            return toDomain.convert(response);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public ListMultipartUploadsDomain listMultipartUploads(ListMultipartUploadsArguments arguments) {
        String function = "listMultipartUploads";
        S3Client client = getClient();
        try {
            Converter<ListMultipartUploadsArguments, ListMultipartUploadsRequest> toRequest =
                    new ArgumentsToListMultipartUploadsRequestConverter();
            Converter<ListMultipartUploadsResponse, ListMultipartUploadsDomain> toDomain =
                    new ListMultipartUploadsResponseToDomainConverter();

            ListMultipartUploadsRequest request = toRequest.convert(arguments);
            ListMultipartUploadsResponse response = client.listMultipartUploads(request);
            return toDomain.convert(response);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }
}
