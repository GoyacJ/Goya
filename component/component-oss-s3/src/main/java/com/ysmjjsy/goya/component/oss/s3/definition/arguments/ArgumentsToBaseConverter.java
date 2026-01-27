package com.ysmjjsy.goya.component.oss.s3.definition.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.base.BaseArguments;
import com.ysmjjsy.goya.component.oss.core.core.converter.OssConverter;
import software.amazon.awssdk.services.s3.model.S3Request;

/**
 * <p>基础的统一定义请求参数转换为 S3 参数转换器</p>
 *
 * @author goya
 * @since 2023/8/10 15:33
 */
public abstract class ArgumentsToBaseConverter<S extends BaseArguments, T extends S3Request> implements OssConverter<S, T> {

    @Override
    public void prepare(S arguments, T request) {

    }
}
