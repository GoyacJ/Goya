package com.ysmjjsy.goya.component.oss.minio.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.object.GeneratePresignedUrlArguments;
import com.ysmjjsy.goya.component.oss.minio.definition.arguments.ArgumentsToObjectVersionConverter;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;

/**
 * <p>统一定义 GeneratePresignedUrlArguments 转 Minio GetPreSignedObjectUrlArgs 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:13
 */
public class ArgumentsToGetPreSignedObjectUrlConverter extends ArgumentsToObjectVersionConverter<GeneratePresignedUrlArguments, GetPresignedObjectUrlArgs, GetPresignedObjectUrlArgs.Builder> {

    @Override
    public void prepare(GeneratePresignedUrlArguments arguments, GetPresignedObjectUrlArgs.Builder builder) {

        builder.method(Method.valueOf(arguments.getMethod().name()));
        builder.expiry(Math.toIntExact(arguments.getExpiration().toSeconds()));
        builder.versionId(arguments.getVersionId());

        super.prepare(arguments, builder);
    }

    @Override
    public GetPresignedObjectUrlArgs.Builder getBuilder() {
        return GetPresignedObjectUrlArgs.builder();
    }
}
