package com.ysmjjsy.goya.component.oss.minio.converter.retention;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.oss.minio.domain.RetentionDomain;
import com.ysmjjsy.goya.component.oss.minio.enums.RetentionModeEnums;
import io.minio.messages.Retention;
import io.minio.messages.RetentionMode;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio Retention 转 RetentionDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:28
 */
public class RetentionToDomainConverter implements Converter<Retention, RetentionDomain> {

    private final Converter<RetentionMode, RetentionModeEnums> toEnums;

    public RetentionToDomainConverter() {
        this.toEnums = new RetentionModeToEnumConverter();
    }

    @Override
    public RetentionDomain convert(Retention retention) {

        RetentionDomain retentionDomain = new RetentionDomain();
        if (ObjectUtils.isNotEmpty(retention)) {
            retentionDomain.setMode(toEnums.convert(retention.mode()));
            if (ObjectUtils.isNotEmpty(retention.retainUntilDate())) {
                retentionDomain.setRetainUntilDate(GoyaDateUtils.zonedDateTimeToString(retention.retainUntilDate()));
            }
            return retentionDomain;
        }

        return null;
    }
}
