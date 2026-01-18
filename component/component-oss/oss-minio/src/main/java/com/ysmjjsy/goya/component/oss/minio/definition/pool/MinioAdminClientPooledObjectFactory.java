package com.ysmjjsy.goya.component.oss.minio.definition.pool;

import com.ysmjjsy.goya.component.oss.core.client.AbstractOssClientPooledObjectFactory;
import com.ysmjjsy.goya.component.oss.minio.configuration.properties.MinioProperties;
import io.minio.admin.MinioAdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Minio 基础 Admin Client 池化工厂</p>
 *
 * @author goya
 * @since 2025/11/1 16:07
 */
public class MinioAdminClientPooledObjectFactory extends AbstractOssClientPooledObjectFactory<MinioAdminClient> {

    private static final Logger log = LoggerFactory.getLogger(MinioAdminClientPooledObjectFactory.class);

    private final MinioProperties minioProperties;

    public MinioAdminClientPooledObjectFactory(MinioProperties minioProperties) {
        super(minioProperties);
        this.minioProperties = minioProperties;
    }

    @Override
    public MinioAdminClient create() throws Exception {
        log.debug("[Goya] |- Minio admin client factory create object.");
        return MinioAdminClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}
