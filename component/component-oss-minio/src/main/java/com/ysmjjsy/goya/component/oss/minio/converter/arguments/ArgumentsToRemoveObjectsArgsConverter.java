package com.ysmjjsy.goya.component.oss.minio.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.object.DeleteObjectsArguments;
import com.ysmjjsy.goya.component.oss.minio.definition.arguments.ArgumentsToBucketConverter;
import io.minio.RemoveObjectsArgs;
import io.minio.messages.DeleteObject;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

/**
 * <p>统一定义 DeletedObjectArguments 转 Minio RemoveObjectArgs 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:15
 */
public class ArgumentsToRemoveObjectsArgsConverter extends ArgumentsToBucketConverter<DeleteObjectsArguments, RemoveObjectsArgs, RemoveObjectsArgs.Builder> {

    @Override
    public void prepare(DeleteObjectsArguments arguments, RemoveObjectsArgs.Builder builder) {
        if (ObjectUtils.isNotEmpty(arguments.getBypassGovernanceMode())) {
            builder.bypassGovernanceMode(arguments.getBypassGovernanceMode());
        }

        List<DeleteObject> deleteObjects = arguments.getObjects().stream().map(item -> new DeleteObject(item.getObjectName(), item.getVersionId())).toList();
        builder.objects(deleteObjects);

        super.prepare(arguments, builder);
    }

    @Override
    public RemoveObjectsArgs.Builder getBuilder() {
        return RemoveObjectsArgs.builder();
    }
}