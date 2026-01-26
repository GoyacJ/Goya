package com.ysmjjsy.goya.component.oss.minio.converter.retention;

import com.google.common.base.Enums;
import com.ysmjjsy.goya.component.oss.minio.enums.RetentionModeEnums;
import io.minio.messages.RetentionMode;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>RetentionModeEnums 转 RetentionMode 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:28
 */
public class EnumToRetentionModeConverter implements Converter<RetentionModeEnums, RetentionMode> {
    @Override
    public RetentionMode convert(RetentionModeEnums enums) {
        if (ObjectUtils.isNotEmpty(enums)) {
            return Enums.getIfPresent(RetentionMode.class, enums.name()).orNull();
        }

        return null;
    }
}