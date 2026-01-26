package com.ysmjjsy.goya.component.oss.minio.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.object.UploadObjectArguments;
import com.ysmjjsy.goya.component.oss.minio.definition.arguments.ArgumentsToPutObjectBaseConverter;
import io.minio.UploadObjectArgs;

import java.io.IOException;

/**
 * <p>统一定义 PutObjectArguments 转 Minio PutObjectArgs 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:15
 */
public class ArgumentsToUploadObjectArgsConverter extends ArgumentsToPutObjectBaseConverter<UploadObjectArguments, UploadObjectArgs, UploadObjectArgs.Builder> {

    @Override
    public void prepare(UploadObjectArguments arguments, UploadObjectArgs.Builder builder) {

        try {
            builder.filename(arguments.getFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        builder.contentType(arguments.getContentType());

        super.prepare(arguments, builder);
    }

    @Override
    public UploadObjectArgs.Builder getBuilder() {
        return UploadObjectArgs.builder();
    }
}
