package com.ysmjjsy.goya.component.oss.aliyun.definition.pool;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.STSAssumeRoleSessionCredentialsProvider;
import com.ysmjjsy.goya.component.oss.core.client.AbstractOssClientPooledObjectFactory;
import com.ysmjjsy.goya.component.oss.aliyun.configuration.properties.AliyunProperties;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.pool2.PooledObject;

/**
 * <p>Aliyun OSS 基础 Client 池化工厂 </p>
 *
 * @author goya
 * @since 2023/7/23 11:48
 */
public class AliyunClientPooledObjectFactory extends AbstractOssClientPooledObjectFactory<OSS> {

    private final AliyunProperties aliyunProperties;

    public AliyunClientPooledObjectFactory(AliyunProperties aliyunProperties) {
        super(aliyunProperties);
        this.aliyunProperties = aliyunProperties;
    }

    @Override
    public OSS create() throws Exception {

        // 创建STSAssumeRoleSessionCredentialsProvider实例。
        STSAssumeRoleSessionCredentialsProvider credentialsProvider = CredentialsProviderFactory
                .newSTSAssumeRoleSessionCredentialsProvider(
                        aliyunProperties.getRegion(),
                        aliyunProperties.getAccessKey(),
                        aliyunProperties.getSecretKey(),
                        aliyunProperties.getRole());

        ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();

        return new OSSClientBuilder().build(aliyunProperties.getEndpoint(), credentialsProvider, configuration);
    }

    @Override
    public void destroyObject(PooledObject<OSS> p) throws Exception {
        OSS client = p.getObject();
        if (ObjectUtils.isNotEmpty(client)) {
            client.shutdown();
        }
    }
}
