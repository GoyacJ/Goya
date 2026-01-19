package com.ysmjjsy.goya.component.oss.s3.repository;

import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.core.arguments.object.*;
import com.ysmjjsy.goya.component.oss.core.core.repository.OssObjectRepository;
import com.ysmjjsy.goya.component.oss.core.domain.base.ObjectWriteDomain;
import com.ysmjjsy.goya.component.oss.core.domain.object.*;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;

/**
 * <p>Amazon S3 Java OSS API 对象操作实现 </p>
 *
 * @author goya
 * @since 2023/8/9 16:47
 */
@Slf4j
@Service
public class S3ObjectRepository extends BaseS3Service implements OssObjectRepository {

    public S3ObjectRepository(AbstractObjectPool<S3Client> ossClientObjectPool) {
        super(ossClientObjectPool);
    }

    @Override
    public ListObjectsDomain listObjects(ListObjectsArguments arguments) {
        return null;
    }

    @Override
    public ListObjectsV2Domain listObjectsV2(ListObjectsV2Arguments arguments) {
        return null;
    }

    @Override
    public void deleteObject(DeleteObjectArguments arguments) {

    }

    @Override
    public List<DeleteObjectDomain> deleteObjects(DeleteObjectsArguments arguments) {
        return List.of();
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
        return "";
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
