package com.ysmjjsy.goya.component.oss.minio.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.object.PutObjectArguments;
import com.ysmjjsy.goya.component.oss.minio.definition.arguments.ArgumentsToPutObjectBaseConverter;
import io.minio.PutObjectArgs;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>统一定义 PutObjectArguments 转 Minio PutObjectArgs 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:14
 */
public class ArgumentsToPutObjectArgsConverter extends ArgumentsToPutObjectBaseConverter<PutObjectArguments, PutObjectArgs, PutObjectArgs.Builder> {

    @Override
    public void prepare(PutObjectArguments arguments, PutObjectArgs.Builder builder) {
        builder.stream(arguments.getInputStream(), arguments.getObjectSize(), arguments.getPartSize());

        if (StringUtils.isNotBlank(arguments.getContentType())) {
            builder.contentType(arguments.getContentType());
        }

        super.prepare(arguments, builder);
    }

    @Override
    public PutObjectArgs.Builder getBuilder() {
        return PutObjectArgs.builder();
    }
}
