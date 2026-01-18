package com.ysmjjsy.goya.component.oss.minio.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.object.DeleteObjectArguments;
import com.ysmjjsy.goya.component.oss.minio.definition.arguments.ArgumentsToObjectVersionConverter;
import io.minio.RemoveObjectArgs;
import org.apache.commons.lang3.ObjectUtils;

/**
 * <p>统一定义 DeletedObjectArguments 转 Minio RemoveObjectArgs 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:15
 */
public class ArgumentsToRemoveObjectArgsConverter extends ArgumentsToObjectVersionConverter<DeleteObjectArguments, RemoveObjectArgs, RemoveObjectArgs.Builder> {

    @Override
    public void prepare(DeleteObjectArguments arguments, RemoveObjectArgs.Builder builder) {
        if (ObjectUtils.isNotEmpty(arguments.getBypassGovernanceMode())) {
            builder.bypassGovernanceMode(arguments.getBypassGovernanceMode());
        }
        super.prepare(arguments, builder);
    }

    @Override
    public RemoveObjectArgs.Builder getBuilder() {
        return RemoveObjectArgs.builder();
    }
}