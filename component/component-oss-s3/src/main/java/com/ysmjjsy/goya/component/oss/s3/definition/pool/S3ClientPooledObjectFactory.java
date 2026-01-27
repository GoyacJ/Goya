package com.ysmjjsy.goya.component.oss.s3.definition.pool;

import com.ysmjjsy.goya.component.framework.oss.client.AbstractOssClientPooledObjectFactory;
import com.ysmjjsy.goya.component.oss.s3.configuration.properties.S3Properties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * <p>Amazon S3 Client 池化工厂 </p>
 *
 * @author goya
 * @since 2023/7/14 16:34
 */
public class S3ClientPooledObjectFactory extends AbstractOssClientPooledObjectFactory<S3Client> {

    private final S3Properties s3Properties;

    public S3ClientPooledObjectFactory(S3Properties s3Properties) {
        super(s3Properties);
        this.s3Properties = s3Properties;
    }

    @Override
    public S3Client create() throws Exception {

        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3Properties.getAccessKey(), s3Properties.getSecretKey()));

        return S3Client.builder()
                .credentialsProvider(staticCredentialsProvider)
                .endpointOverride(URI.create(s3Properties.getEndpoint()))
                .build();
    }
}
