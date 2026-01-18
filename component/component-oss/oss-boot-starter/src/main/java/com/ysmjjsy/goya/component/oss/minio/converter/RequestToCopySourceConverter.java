package com.ysmjjsy.goya.component.oss.minio.converter;

import com.ysmjjsy.goya.component.oss.minio.request.domain.CopySourceRequest;
import io.minio.CopySource;
import org.springframework.core.convert.converter.Converter;

/**
 * <p> TODO </p>
 *
 * @author goya
 * @since 2023/5/31 14:56
 */
public class RequestToCopySourceConverter implements Converter<CopySourceRequest, CopySource> {
    @Override
    public CopySource convert(CopySourceRequest source) {
        return null;
    }
}
