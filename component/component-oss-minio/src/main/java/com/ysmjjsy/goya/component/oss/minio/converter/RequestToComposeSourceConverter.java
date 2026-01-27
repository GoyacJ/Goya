package com.ysmjjsy.goya.component.oss.minio.converter;

import com.ysmjjsy.goya.component.oss.minio.request.domain.ComposeSourceRequest;
import io.minio.ComposeSource;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio Request 转 ComposeSource 转换器 </p>
 *
 * @author goya
 * @since 2023/5/31 14:48
 */
public class RequestToComposeSourceConverter implements Converter<ComposeSourceRequest, ComposeSource> {
    @Override
    public ComposeSource convert(ComposeSourceRequest source) {
        return null;
    }
}
