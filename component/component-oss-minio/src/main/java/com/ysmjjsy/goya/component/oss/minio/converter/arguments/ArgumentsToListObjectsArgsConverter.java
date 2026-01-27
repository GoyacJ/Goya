package com.ysmjjsy.goya.component.oss.minio.converter.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.object.ListObjectsArguments;
import com.ysmjjsy.goya.component.oss.minio.definition.arguments.ArgumentsToBucketConverter;
import io.minio.ListObjectsArgs;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>统一定义 OssDomain 转 Minio ListObjectsArgs 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:13
 */
public class ArgumentsToListObjectsArgsConverter extends ArgumentsToBucketConverter<ListObjectsArguments, ListObjectsArgs, ListObjectsArgs.Builder> {

    @Override
    public void prepare(ListObjectsArguments arguments, ListObjectsArgs.Builder builder) {
        builder.delimiter(arguments.getDelimiter());
        builder.useUrlEncodingType(StringUtils.isNotBlank(arguments.getEncodingType()));
        builder.maxKeys(arguments.getMaxKeys());
        builder.prefix(arguments.getPrefix());
        builder.recursive(false);
        builder.useApiVersion1(true);

        if (StringUtils.isNotBlank(arguments.getMarker())) {
            builder.keyMarker(arguments.getMarker());
        }

        super.prepare(arguments, builder);
    }

    @Override
    public ListObjectsArgs.Builder getBuilder() {
        return ListObjectsArgs.builder();
    }
}