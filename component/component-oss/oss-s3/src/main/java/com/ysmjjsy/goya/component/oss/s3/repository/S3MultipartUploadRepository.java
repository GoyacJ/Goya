package com.ysmjjsy.goya.component.oss.s3.repository;

import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.core.arguments.multipart.*;
import com.ysmjjsy.goya.component.oss.core.core.repository.OssMultipartUploadRepository;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.*;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * <p>Amazon S3 Java OSS API 分片上传操作实现 </p>
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
       
    }

    @Override
    public UploadPartDomain uploadPart(UploadPartArguments arguments) {
        return null;
    }

    @Override
    public UploadPartCopyDomain uploadPartCopy(UploadPartCopyArguments arguments) {
        return null;
    }

    @Override
    public CompleteMultipartUploadDomain completeMultipartUpload(CompleteMultipartUploadArguments arguments) {
        return null;
    }

    @Override
    public AbortMultipartUploadDomain abortMultipartUpload(AbortMultipartUploadArguments arguments) {
        return null;
    }

    @Override
    public ListPartsDomain listParts(ListPartsArguments arguments) {
        return null;
    }

    @Override
    public ListMultipartUploadsDomain listMultipartUploads(ListMultipartUploadsArguments arguments) {
        return null;
    }
}
