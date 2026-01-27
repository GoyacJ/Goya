package com.ysmjjsy.goya.component.oss.s3.repository;

import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.core.arguments.object.*;
import com.ysmjjsy.goya.component.oss.core.core.repository.OssObjectRepository;
import com.ysmjjsy.goya.component.oss.core.domain.base.ObjectWriteDomain;
import com.ysmjjsy.goya.component.oss.core.domain.object.*;
import com.ysmjjsy.goya.component.oss.core.enums.HttpMethodEnum;
import com.ysmjjsy.goya.component.oss.core.exception.OssException;
import com.ysmjjsy.goya.component.oss.s3.configuration.properties.S3Properties;
import com.ysmjjsy.goya.component.oss.s3.converter.arguments.*;
import com.ysmjjsy.goya.component.oss.s3.converter.domain.*;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

/**
 * <p>Amazon S3 Java OSS API 对象操作实现</p>
 *
 * @author goya
 * @since 2023/8/9 16:47
 */
@Slf4j
@Service
public class S3ObjectRepository extends BaseS3Service implements OssObjectRepository {

    private final S3Presigner presigner;

    public S3ObjectRepository(AbstractObjectPool<S3Client> ossClientObjectPool, S3Properties s3Properties) {
        super(ossClientObjectPool);
        this.presigner = createPresigner(s3Properties);
    }

    private S3Presigner createPresigner(S3Properties s3Properties) {
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3Properties.getAccessKey(), s3Properties.getSecretKey()));

        S3Presigner.Builder builder = S3Presigner.builder()
                .credentialsProvider(credentialsProvider);

        if (s3Properties.getEndpoint() != null) {
            builder.endpointOverride(URI.create(s3Properties.getEndpoint()));
        }

        return builder.build();
    }

    @Override
    public ListObjectsDomain listObjects(ListObjectsArguments arguments) {
        String function = "listObjects";
        S3Client client = getClient();
        try {
            Converter<ListObjectsArguments, ListObjectsRequest> toRequest =
                    new ArgumentsToListObjectsRequestConverter();
            Converter<ListObjectsResponse, ListObjectsDomain> toDomain =
                    new ListObjectsResponseToDomainConverter();

            ListObjectsRequest request = toRequest.convert(arguments);
            ListObjectsResponse response = client.listObjects(request);
            return toDomain.convert(response);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public ListObjectsV2Domain listObjectsV2(ListObjectsV2Arguments arguments) {
        String function = "listObjectsV2";
        S3Client client = getClient();
        try {
            Converter<ListObjectsV2Arguments, ListObjectsV2Request> toRequest =
                    new ArgumentsToListObjectsV2RequestConverter();
            Converter<ListObjectsV2Response, ListObjectsV2Domain> toDomain =
                    new ListObjectsV2ResponseToDomainConverter();

            ListObjectsV2Request request = toRequest.convert(arguments);
            ListObjectsV2Response response = client.listObjectsV2(request);
            return toDomain.convert(response);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public void deleteObject(DeleteObjectArguments arguments) {
        String function = "deleteObject";
        S3Client client = getClient();
        try {
            Converter<DeleteObjectArguments, DeleteObjectRequest> toRequest =
                    new ArgumentsToDeleteObjectRequestConverter();

            DeleteObjectRequest request = toRequest.convert(arguments);
            client.deleteObject(request);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public List<DeleteObjectDomain> deleteObjects(DeleteObjectsArguments arguments) {
        String function = "deleteObjects";
        S3Client client = getClient();
        try {
            Converter<DeleteObjectsArguments, DeleteObjectsRequest> toRequest =
                    new ArgumentsToDeleteObjectsRequestConverter();
            Converter<DeleteObjectsResponse, List<DeleteObjectDomain>> toDomain =
                    new DeleteObjectsResponseToDomainConverter();

            DeleteObjectsRequest request = toRequest.convert(arguments);
            DeleteObjectsResponse response = client.deleteObjects(request);
            return toDomain.convert(response);
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public ObjectMetadataDomain getObjectMetadata(GetObjectMetadataArguments arguments) {
        String function = "getObjectMetadata";
        S3Client client = getClient();
        try {
            Converter<GetObjectMetadataArguments, HeadObjectRequest> toRequest =
                    new ArgumentsToHeadObjectRequestConverter();
            Converter<HeadObjectResponse, ObjectMetadataDomain> toDomain =
                    new HeadObjectResponseToDomainConverter();

            HeadObjectRequest request = toRequest.convert(arguments);
            HeadObjectResponse response = client.headObject(request);
            
            ObjectMetadataDomain domain = toDomain.convert(response);
            domain.setBucketName(arguments.getBucketName());
            domain.setObjectName(arguments.getObjectName());
            
            return domain;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public GetObjectDomain getObject(GetObjectArguments arguments) {
        String function = "getObject";
        S3Client client = getClient();
        try {
            Converter<GetObjectArguments, GetObjectRequest> toRequest =
                    new ArgumentsToGetObjectRequestConverter();
            Converter<ResponseInputStream<GetObjectResponse>, GetObjectDomain> toDomain =
                    new GetObjectResponseToDomainConverter();

            GetObjectRequest request = toRequest.convert(arguments);
            ResponseInputStream<GetObjectResponse> response = client.getObject(request);
            
            GetObjectDomain domain = toDomain.convert(response);
            domain.setBucketName(arguments.getBucketName());
            domain.setObjectName(arguments.getObjectName());
            
            return domain;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public PutObjectDomain putObject(PutObjectArguments arguments) {
        String function = "putObject";
        S3Client client = getClient();
        try {
            Converter<PutObjectArguments, PutObjectRequest> toRequest =
                    new ArgumentsToPutObjectRequestConverter();
            Converter<PutObjectResponse, PutObjectDomain> toDomain =
                    new PutObjectResponseToDomainConverter();

            PutObjectRequest request = toRequest.convert(arguments);
            RequestBody requestBody = RequestBody.fromInputStream(
                    arguments.getInputStream(),
                    arguments.getObjectSize());

            PutObjectResponse response = client.putObject(request, requestBody);
            
            PutObjectDomain domain = toDomain.convert(response);
            domain.setBucketName(arguments.getBucketName());
            domain.setObjectName(arguments.getObjectName());
            
            return domain;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } finally {
            close(client);
        }
    }

    @Override
    public String generatePresignedUrl(GeneratePresignedUrlArguments arguments) {
        String function = "generatePresignedUrl";
        try {
            Duration expiration = arguments.getExpiration() != null 
                    ? arguments.getExpiration() 
                    : Duration.ofDays(7);

            if (arguments.getMethod() == HttpMethodEnum.GET) {
                GetObjectRequest.Builder getRequestBuilder = GetObjectRequest.builder()
                        .bucket(arguments.getBucketName())
                        .key(arguments.getObjectName());

                if (arguments.getVersionId() != null) {
                    getRequestBuilder.versionId(arguments.getVersionId());
                }

                GetObjectRequest getRequest = getRequestBuilder.build();

                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .getObjectRequest(getRequest)
                        .build();

                PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
                return presignedRequest.url().toString();
            } else if (arguments.getMethod() == HttpMethodEnum.PUT) {
                PutObjectRequest.Builder putRequestBuilder = PutObjectRequest.builder()
                        .bucket(arguments.getBucketName())
                        .key(arguments.getObjectName());

                if (arguments.getContentType() != null) {
                    putRequestBuilder.contentType(arguments.getContentType());
                }

                PutObjectRequest putRequest = putRequestBuilder.build();

                PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .putObjectRequest(putRequest)
                        .build();

                PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
                return presignedRequest.url().toString();
            } else {
                throw new IllegalArgumentException("不支持的HTTP方法: " + arguments.getMethod());
            }
        } catch (Exception e) {
            log.error("[Goya] |- S3 OSS catch Exception in [{}].", function, e);
            throw e;
        }
    }

    @Override
    public ObjectMetadataDomain download(DownloadObjectArguments arguments) {
        String function = "download";
        S3Client client = getClient();
        try {
            Converter<GetObjectArguments, GetObjectRequest> toRequest =
                    new ArgumentsToGetObjectRequestConverter();
            Converter<HeadObjectResponse, ObjectMetadataDomain> toDomain =
                    new HeadObjectResponseToDomainConverter();

            GetObjectRequest request = toRequest.convert(arguments);
            Path filePath = Paths.get(arguments.getFilename());

            if (!arguments.getOverwrite() && Files.exists(filePath)) {
                throw new IllegalStateException("文件已存在: " + arguments.getFilename());
            }

            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            client.getObject(request, ResponseTransformer.toFile(filePath));

            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(arguments.getBucketName())
                    .key(arguments.getObjectName())
                    .versionId(arguments.getVersionId())
                    .build();

            HeadObjectResponse headResponse = client.headObject(headRequest);
            
            ObjectMetadataDomain domain = toDomain.convert(headResponse);
            domain.setBucketName(arguments.getBucketName());
            domain.setObjectName(arguments.getObjectName());
            
            return domain;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } catch (Exception e) {
            log.error("[Goya] |- S3 OSS catch Exception in [{}].", function, e);
            throw new OssException("下载对象失败", e);
        } finally {
            close(client);
        }
    }

    @Override
    public ObjectWriteDomain upload(UploadObjectArguments arguments) {
        String function = "upload";
        S3Client client = getClient();
        try {
            Converter<PutObjectArguments, PutObjectRequest> toRequest =
                    new ArgumentsToPutObjectRequestConverter();
            Converter<PutObjectResponse, PutObjectDomain> toDomain =
                    new PutObjectResponseToDomainConverter();

            PutObjectArguments putArgs = new PutObjectArguments();
            putArgs.setBucketName(arguments.getBucketName());
            putArgs.setObjectName(arguments.getObjectName());
            putArgs.setContentType(arguments.getContentType());
            putArgs.setObjectSize(arguments.getObjectSize());
            putArgs.setPartSize(arguments.getPartSize());
            putArgs.setMetadata(arguments.getMetadata());
            putArgs.setRequestHeaders(arguments.getRequestHeaders());

            Path filePath = Paths.get(arguments.getFilename());
            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("文件不存在: " + arguments.getFilename());
            }

            PutObjectRequest request = toRequest.convert(putArgs);
            RequestBody requestBody = RequestBody.fromFile(filePath);

            PutObjectResponse response = client.putObject(request, requestBody);
            
            PutObjectDomain domain = toDomain.convert(response);
            domain.setBucketName(arguments.getBucketName());
            domain.setObjectName(arguments.getObjectName());
            
            return domain;
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } catch (Exception e) {
            log.error("[Goya] |- S3 OSS catch Exception in [{}].", function, e);
            throw new OssException("上传对象失败", e);
        } finally {
            close(client);
        }
    }
}
