package com.ysmjjsy.goya.component.oss.minio.converter.sse;

import com.google.common.base.Enums;
import com.ysmjjsy.goya.component.oss.minio.enums.SseConfigurationEnums;
import io.minio.messages.SseConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio SseConfiguration 转 SseConfigurationEnums 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:30
 */
public class SseConfigurationToEnumConverter implements Converter<SseConfiguration, SseConfigurationEnums> {
    @Override
    public SseConfigurationEnums convert(SseConfiguration configuration) {
        if (ObjectUtils.isNotEmpty(configuration) && ObjectUtils.isNotEmpty(configuration.rule())) {
            return Enums.getIfPresent(SseConfigurationEnums.class, configuration.rule().sseAlgorithm().name()).or(SseConfigurationEnums.AES256);
        }

        return SseConfigurationEnums.DISABLED;
    }
}
