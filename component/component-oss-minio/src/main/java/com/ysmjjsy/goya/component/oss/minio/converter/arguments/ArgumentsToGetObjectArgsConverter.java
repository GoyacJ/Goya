package com.ysmjjsy.goya.component.oss.minio.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.object.GetObjectArguments;
import com.ysmjjsy.goya.component.oss.minio.definition.arguments.ArgumentsToObjectConditionalReadConverter;
import io.minio.GetObjectArgs;

/**
 * <p>统一定义 GetObjectArguments 转 Minio GetObjectArgs 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:13
 */
public class ArgumentsToGetObjectArgsConverter extends ArgumentsToObjectConditionalReadConverter<GetObjectArguments, GetObjectArgs, GetObjectArgs.Builder> {

    @Override
    public GetObjectArgs.Builder getBuilder() {
        return GetObjectArgs.builder();
    }
}
