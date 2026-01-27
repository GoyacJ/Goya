package com.ysmjjsy.goya.component.oss.minio.repository;

import com.ysmjjsy.goya.component.framework.oss.arguments.multipart.*;
import com.ysmjjsy.goya.component.framework.oss.core.repository.OssMultipartUploadRepository;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.*;
import com.ysmjjsy.goya.component.oss.minio.converter.domain.*;
import com.ysmjjsy.goya.component.oss.minio.service.MinioMultipartUploadService;
import io.minio.*;
import io.minio.messages.InitiateMultipartUploadResult;
import io.minio.messages.ListMultipartUploadsResult;
import io.minio.messages.ListPartsResult;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Minio Java OSS API 分片上传操作实现</p>
 *
 * @author goya
 * @since 2025/11/1 16:33
 */
@Slf4j
@Service
public class MinioMultipartUploadRepository implements OssMultipartUploadRepository {

    private final MinioMultipartUploadService minioMultipartUploadService;

    public MinioMultipartUploadRepository(MinioMultipartUploadService minioMultipartUploadService) {
        this.minioMultipartUploadService = minioMultipartUploadService;
    }

    @Override
    public InitiateMultipartUploadDomain initiateMultipartUpload(InitiateMultipartUploadArguments arguments) {

        Converter<InitiateMultipartUploadResult, InitiateMultipartUploadDomain> toDomain = new InitiateMultipartUploadResultToDomainConverter();

        CreateMultipartUploadResponse response = minioMultipartUploadService.createMultipartUpload(
                arguments.getBucketName(),
                arguments.getRegion(),
                arguments.getObjectName(),
                arguments.getExtraHeaders(),
                arguments.getExtraQueryParams());

        return toDomain.convert(response.result());
    }

    @Override
    public UploadPartDomain uploadPart(UploadPartArguments arguments) {

        Converter<UploadPartResponse, UploadPartDomain> toDomain = new UploadPartResponseToDomainConverter();

        UploadPartResponse response = minioMultipartUploadService.uploadPart(
                arguments.getBucketName(),
                arguments.getRegion(),
                arguments.getObjectName(),
                arguments.getInputStream(),
                arguments.getPartSize(),
                arguments.getUploadId(),
                arguments.getPartNumber(),
                arguments.getExtraHeaders(),
                arguments.getExtraQueryParams());

        return toDomain.convert(response);
    }

    @Override
    public UploadPartCopyDomain uploadPartCopy(UploadPartCopyArguments arguments) {

        Converter<UploadPartCopyResponse, UploadPartCopyDomain> toDomain = new UploadPartCopyResponseToDomainConverter();

        UploadPartCopyResponse response = minioMultipartUploadService.uploadPartCopy(
                arguments.getBucketName(),
                arguments.getRegion(),
                arguments.getObjectName(),
                arguments.getUploadId(),
                arguments.getPartNumber(),
                arguments.getExtraHeaders(),
                arguments.getExtraQueryParams());

        return toDomain.convert(response);
    }

    @Override
    public CompleteMultipartUploadDomain completeMultipartUpload(CompleteMultipartUploadArguments arguments) {

        Converter<List<PartSummaryDomain>, Part[]> toPart = new DomainToPartConverter();
        Converter<ObjectWriteResponse, CompleteMultipartUploadDomain> toDomain = new ObjectWriteResponseToCompleteMultipartUploadDomainConverter();

        ObjectWriteResponse response = minioMultipartUploadService.completeMultipartUpload(
                arguments.getBucketName(),
                arguments.getRegion(),
                arguments.getObjectName(),
                arguments.getUploadId(),
                toPart.convert(arguments.getParts()),
                arguments.getExtraHeaders(),
                arguments.getExtraQueryParams());

        return toDomain.convert(response);
    }

    @Override
    public AbortMultipartUploadDomain abortMultipartUpload(AbortMultipartUploadArguments arguments) {

        Converter<AbortMultipartUploadResponse, AbortMultipartUploadDomain> toDomain = new AbortMultipartUploadResponseToDomainConverter();

        AbortMultipartUploadResponse response = minioMultipartUploadService.abortMultipartUpload(
                arguments.getBucketName(),
                arguments.getRegion(),
                arguments.getObjectName(),
                arguments.getUploadId(),
                arguments.getExtraHeaders(),
                arguments.getExtraQueryParams());

        return toDomain.convert(response);
    }

    @Override
    public ListPartsDomain listParts(ListPartsArguments arguments) {

        Converter<ListPartsResult, ListPartsDomain> toDomain = new ListPartsResultToDomainConverter(arguments);

        ListPartsResponse response = minioMultipartUploadService.listParts(
                arguments.getBucketName(),
                arguments.getRegion(),
                arguments.getObjectName(),
                arguments.getMaxParts(),
                arguments.getPartNumberMarker(),
                arguments.getUploadId(),
                arguments.getExtraHeaders(),
                arguments.getExtraQueryParams());

        return toDomain.convert(response.result());
    }

    @Override
    public ListMultipartUploadsDomain listMultipartUploads(ListMultipartUploadsArguments arguments) {

        Converter<ListMultipartUploadsResult, ListMultipartUploadsDomain> toDomain = new ListMultipartUploadsResultToDomainConverter(arguments);

        ListMultipartUploadsResponse response = minioMultipartUploadService.listMultipartUploads(
                arguments.getBucketName(),
                arguments.getRegion(),
                arguments.getDelimiter(),
                arguments.getEncodingType(),
                arguments.getKeyMarker(),
                arguments.getMaxUploads(),
                arguments.getPrefix(),
                arguments.getUploadIdMarker(),
                arguments.getExtraHeaders(),
                arguments.getExtraQueryParams());

        return toDomain.convert(response.result());
    }
}
