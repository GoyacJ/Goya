package com.ysmjjsy.goya.component.oss.minio.definition.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.base.ObjectVersionArguments;
import io.minio.ObjectVersionArgs;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>统一定义对象版本请求参数转换为 Minio 参数转换器</p>
 *
 * @author goya
 * @since 2025/11/1 15:58
 */
public abstract class ArgumentsToObjectVersionConverter<S extends ObjectVersionArguments, T extends ObjectVersionArgs, B extends ObjectVersionArgs.Builder<B, T>> extends ArgumentsToObjectConverter<S, T, B> {
    @Override
    public void prepare(S arguments, B builder) {
        if (StringUtils.isNotBlank(arguments.getVersionId())) {
            builder.versionId(arguments.getVersionId());
        }
        super.prepare(arguments, builder);
    }
}