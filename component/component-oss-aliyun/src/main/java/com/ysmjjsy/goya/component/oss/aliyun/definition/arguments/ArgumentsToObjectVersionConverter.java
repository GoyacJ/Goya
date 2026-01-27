package com.ysmjjsy.goya.component.oss.aliyun.definition.arguments;

import com.aliyun.oss.model.GenericRequest;
import com.ysmjjsy.goya.component.oss.core.arguments.base.ObjectVersionArguments;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>统一定义对象版本请求参数转换为 Aliyun 参数转换器 </p>
 *
 * @author goya
 * @since 2023/8/15 12:30
 */
public abstract class ArgumentsToObjectVersionConverter<S extends ObjectVersionArguments, T extends GenericRequest> extends ArgumentsToObjectConverter<S, T> {

    @Override
    public void prepare(S arguments, T request) {
        request.setBucketName(arguments.getBucketName());
        request.setKey(arguments.getObjectName());

        if (StringUtils.isNotBlank(arguments.getVersionId())) {
            request.setVersionId(arguments.getVersionId());
        }

        super.prepare(arguments, request);
    }
}
