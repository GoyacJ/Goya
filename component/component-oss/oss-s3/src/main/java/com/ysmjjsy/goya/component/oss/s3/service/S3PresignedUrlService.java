package com.ysmjjsy.goya.component.oss.s3.service;

import com.ysmjjsy.goya.component.core.pool.AbstractObjectPool;
import com.ysmjjsy.goya.component.oss.s3.configuration.properties.S3Properties;
import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.service.BaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;

/**
 * <p>Presigned Url Service </p>
 *
 * @author goya
 * @since 2023/7/16 22:30
 */
@Slf4j
@Service
public class S3PresignedUrlService extends BaseS3Service {

    private final S3Presigner presigner;

    public S3PresignedUrlService(AbstractObjectPool<S3Client> s3ClientObjectPool, S3Properties s3Properties) {
        super(s3ClientObjectPool);
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

    /**
     * Presigned Url download
     *
     * @param request GetObjectPresignRequest请求
     * @return 预签名的GET URL字符串
     * @throws S3Exception S3操作异常
     */
    public String presignGetObject(GetObjectPresignRequest request) {
        String function = "presignGetObject";
        try {
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(request);
            return presignedRequest.url().toString();
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } catch (Exception e) {
            log.error("[Goya] |- S3 OSS catch Exception in [{}].", function, e);
            throw new RuntimeException("生成预签名URL失败", e);
        }
    }

    /**
     * Presigned Url upload
     *
     * @param request PutObjectPresignRequest请求
     * @return 预签名的PUT URL字符串
     * @throws S3Exception S3操作异常
     */
    public String presignPutObject(PutObjectPresignRequest request) {
        String function = "presignPutObject";
        try {
            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(request);
            return presignedRequest.url().toString();
        } catch (S3Exception e) {
            log.error("[Goya] |- S3 OSS catch S3Exception in [{}].", function, e);
            throw e;
        } catch (Exception e) {
            log.error("[Goya] |- S3 OSS catch Exception in [{}].", function, e);
            throw new RuntimeException("生成预签名URL失败", e);
        }
    }
}
