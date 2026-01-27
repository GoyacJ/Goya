package com.ysmjjsy.goya.component.oss.minio.definition.pool;

import com.ysmjjsy.goya.component.oss.core.client.AbstractOssClientPooledObjectFactory;
import com.ysmjjsy.goya.component.oss.minio.configuration.properties.MinioProperties;

/**
 * <p>扩展的 Minio 异步 Client 池化工厂</p>
 *
 * @author goya
 * @since 2025/11/1 16:09
 */
public class MinioAsyncClientPooledObjectFactory extends AbstractOssClientPooledObjectFactory<MinioAsyncClient> {

    private final MinioProperties minioProperties;

    public MinioAsyncClientPooledObjectFactory(MinioProperties minioProperties) {
        super(minioProperties);
        this.minioProperties = minioProperties;
    }

    @Override
    public MinioAsyncClient create() throws Exception {
        io.minio.MinioAsyncClient minioAsyncClient = io.minio.MinioAsyncClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        return new MinioAsyncClient(minioAsyncClient);
    }
}