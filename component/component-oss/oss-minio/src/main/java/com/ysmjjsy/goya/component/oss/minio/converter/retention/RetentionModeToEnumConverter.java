package com.ysmjjsy.goya.component.oss.minio.converter.retention;

import com.ysmjjsy.goya.component.oss.minio.enums.RetentionModeEnums;
import io.minio.messages.RetentionMode;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio RetentionMode 转 RetentionModeEnums 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:28
 */
public class RetentionModeToEnumConverter implements Converter<RetentionMode, RetentionModeEnums> {

    @Override
    public RetentionModeEnums convert(RetentionMode retentionMode) {

        if (ObjectUtils.isNotEmpty(retentionMode)) {
            String retentionModeName = retentionMode.name();
            return RetentionModeEnums.valueOf(retentionModeName);
        }

        return null;
    }
}
