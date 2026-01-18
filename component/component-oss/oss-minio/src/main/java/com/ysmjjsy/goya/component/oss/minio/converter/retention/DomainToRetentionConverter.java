package com.ysmjjsy.goya.component.oss.minio.converter.retention;

import com.ysmjjsy.goya.component.core.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.oss.minio.domain.RetentionDomain;
import com.ysmjjsy.goya.component.oss.minio.enums.RetentionModeEnums;
import io.minio.messages.Retention;
import io.minio.messages.RetentionMode;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

import java.time.ZonedDateTime;

/**
 * <p>Minio Request 转 Retention 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:27
 */
public class DomainToRetentionConverter implements Converter<RetentionDomain, Retention> {

    private final Converter<RetentionModeEnums, RetentionMode> toRetentionMode = new EnumToRetentionModeConverter();

    @Override
    public Retention convert(RetentionDomain retentionDomain) {
        RetentionMode mode = toRetentionMode.convert(retentionDomain.getMode());
        ZonedDateTime retainUntilDate = GoyaDateUtils.stringToZonedDateTime(retentionDomain.getRetainUntilDate());
        if (ObjectUtils.isNotEmpty(mode) && ObjectUtils.isNotEmpty(retainUntilDate)) {
            return new Retention(mode, retainUntilDate);
        } else {
            return null;
        }
    }
}
