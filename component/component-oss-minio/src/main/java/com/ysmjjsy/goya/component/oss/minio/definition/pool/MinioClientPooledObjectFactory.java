package com.ysmjjsy.goya.component.oss.minio.definition.pool;

import com.ysmjjsy.goya.component.oss.core.client.AbstractOssClientPooledObjectFactory;
import com.ysmjjsy.goya.component.oss.minio.configuration.properties.MinioProperties;
import io.minio.MinioClient;

/**
 * <p>Minio 基础 Client 池化工厂</p>
 *
 * @author goya
 * @since 2025/11/1 16:10
 */
public class MinioClientPooledObjectFactory extends AbstractOssClientPooledObjectFactory<MinioClient> {

    private final MinioProperties minioProperties;

    public MinioClientPooledObjectFactory(MinioProperties minioProperties) {
        super(minioProperties);
        this.minioProperties = minioProperties;
    }

    @Override
    public MinioClient create() throws Exception {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}
