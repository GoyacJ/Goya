package com.ysmjjsy.goya.component.oss.minio.converter.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.object.DownloadObjectArguments;
import com.ysmjjsy.goya.component.oss.minio.definition.arguments.ArgumentsToObjectReadConverter;
import io.minio.DownloadObjectArgs;

/**
 * <p>统一定义 GetObjectArguments 转 Minio GetObjectArgs 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:13
 */
public class ArgumentsToDownloadObjectArgsConverter extends ArgumentsToObjectReadConverter<DownloadObjectArguments, DownloadObjectArgs, DownloadObjectArgs.Builder> {

    @Override
    public void prepare(DownloadObjectArguments arguments, DownloadObjectArgs.Builder builder) {
        builder.filename(arguments.getFilename());
        builder.overwrite(arguments.getOverwrite());

        super.prepare(arguments, builder);
    }

    @Override
    public DownloadObjectArgs.Builder getBuilder() {
        return DownloadObjectArgs.builder();
    }
}