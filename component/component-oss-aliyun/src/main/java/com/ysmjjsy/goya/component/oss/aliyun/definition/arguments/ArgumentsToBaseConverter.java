package com.ysmjjsy.goya.component.oss.aliyun.definition.arguments;

import com.aliyun.oss.model.WebServiceRequest;
import com.ysmjjsy.goya.component.oss.core.arguments.base.BaseArguments;
import com.ysmjjsy.goya.component.oss.core.core.converter.OssConverter;
import org.apache.commons.collections4.MapUtils;

/**
 * <p>基础的统一定义请求参数转换为 Aliyun 参数转换器 </p>
 *
 * @author goya
 * @since 2023/8/10 15:33
 */
public abstract class ArgumentsToBaseConverter<S extends BaseArguments, T extends WebServiceRequest> implements OssConverter<S, T> {

    @Override
    public void prepare(S arguments, T request) {
        if (MapUtils.isNotEmpty(arguments.getExtraHeaders())) {
            request.setHeaders(arguments.getExtraHeaders());
        }

        if (MapUtils.isNotEmpty(arguments.getExtraQueryParams())) {
            request.setParameters(arguments.getExtraQueryParams());
        }
    }
}
