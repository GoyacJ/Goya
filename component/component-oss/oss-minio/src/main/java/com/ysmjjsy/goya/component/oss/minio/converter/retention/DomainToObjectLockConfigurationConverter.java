package com.ysmjjsy.goya.component.oss.minio.converter.retention;

import com.ysmjjsy.goya.component.oss.minio.domain.ObjectLockConfigurationDomain;
import com.ysmjjsy.goya.component.oss.minio.enums.RetentionModeEnums;
import com.ysmjjsy.goya.component.oss.minio.enums.RetentionUnitEnums;
import io.minio.messages.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio Request 转 ObjectLockConfiguration 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:27
 */
public class DomainToObjectLockConfigurationConverter implements Converter<ObjectLockConfigurationDomain, ObjectLockConfiguration> {

    private final Converter<RetentionModeEnums, RetentionMode> toRetentionMode = new EnumToRetentionModeConverter();

    @Override
    public ObjectLockConfiguration convert(ObjectLockConfigurationDomain source) {

        if (isRetentionModeValid(source) && isRetentionDurationModeValid(source)) {
            RetentionMode mode = toRetentionMode.convert(source.getMode());
            RetentionDuration duration = getRetentionDuration(source.getUnit(), source.getValidity());
            return new ObjectLockConfiguration(mode, duration);
        }

        return null;
    }

    private boolean isRetentionModeValid(ObjectLockConfigurationDomain source) {
        RetentionModeEnums enums = source.getMode();
        return ObjectUtils.isNotEmpty(enums);
    }

    private boolean isRetentionDurationModeValid(ObjectLockConfigurationDomain source) {
        RetentionUnitEnums enums = source.getUnit();
        Integer duration = source.getValidity();
        return ObjectUtils.isNotEmpty(enums) && ObjectUtils.isNotEmpty(duration) && duration != 0;
    }

    private RetentionDuration getRetentionDuration(RetentionUnitEnums enums, Integer duration) {
        if (enums == RetentionUnitEnums.DAYS) {
            return new RetentionDurationDays(duration);
        } else {
            return new RetentionDurationYears(duration);
        }
    }
}