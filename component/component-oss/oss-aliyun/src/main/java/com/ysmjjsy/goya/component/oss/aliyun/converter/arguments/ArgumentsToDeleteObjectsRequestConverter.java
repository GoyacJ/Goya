package com.ysmjjsy.goya.component.oss.aliyun.converter.arguments;

import com.aliyun.oss.model.DeleteObjectsRequest;
import com.ysmjjsy.goya.component.oss.core.arguments.object.DeleteObjectsArguments;
import com.ysmjjsy.goya.component.oss.core.arguments.object.DeletedObjectArguments;
import com.ysmjjsy.goya.component.oss.aliyun.definition.arguments.ArgumentsToBucketConverter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * <p>统一定义 DeleteObjectsArguments 转 Aliyun DeleteObjectsRequest 转换器 </p>
 *
 * @author goya
 * @since 2023/7/28 19:59
 */
public class ArgumentsToDeleteObjectsRequestConverter extends ArgumentsToBucketConverter<DeleteObjectsArguments, DeleteObjectsRequest> {

    @Override
    public void prepare(DeleteObjectsArguments arguments, DeleteObjectsRequest request) {
        List<DeletedObjectArguments> keys = arguments.getObjects();
        if (CollectionUtils.isNotEmpty(keys)) {
            List<String> values = keys.stream().map(DeletedObjectArguments::getObjectName).toList();
            request.setKeys(values);
        }

        request.setQuiet(arguments.getQuiet());
        super.prepare(arguments, request);
    }

    @Override
    public DeleteObjectsRequest getInstance(DeleteObjectsArguments arguments) {
        return new DeleteObjectsRequest(arguments.getBucketName());
    }
}
