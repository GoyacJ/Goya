package com.ysmjjsy.goya.component.oss.minio.converter.retention;

import com.ysmjjsy.goya.component.oss.minio.domain.VersioningConfigurationDomain;
import io.minio.messages.VersioningConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio VersioningConfiguration 转 VersioningConfigurationDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:29
 */
public class VersioningConfigurationToDomainConverter implements Converter<VersioningConfiguration, VersioningConfigurationDomain> {
    @Override
    public VersioningConfigurationDomain convert(VersioningConfiguration versioningConfiguration) {

        if (ObjectUtils.isNotEmpty(versioningConfiguration)) {
            VersioningConfigurationDomain domain = new VersioningConfigurationDomain();
            domain.setStatus(versioningConfiguration.status().name());
            domain.setMfaDelete(versioningConfiguration.isMfaDeleteEnabled());
            return domain;
        }

        return null;
    }
}