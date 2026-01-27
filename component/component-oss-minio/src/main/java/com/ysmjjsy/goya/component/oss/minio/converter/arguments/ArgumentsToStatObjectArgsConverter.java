package com.ysmjjsy.goya.component.oss.minio.converter.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.object.GetObjectMetadataArguments;
import com.ysmjjsy.goya.component.oss.minio.definition.arguments.ArgumentsToObjectConditionalReadConverter;
import io.minio.StatObjectArgs;

/**
 * <p>统一定义 GetObjectMetadataArguments 转 Minio StatObjectArgs 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:15
 */
public class ArgumentsToStatObjectArgsConverter extends ArgumentsToObjectConditionalReadConverter<GetObjectMetadataArguments, StatObjectArgs, StatObjectArgs.Builder> {
    @Override
    public StatObjectArgs.Builder getBuilder() {
        return StatObjectArgs.builder();
    }
}
