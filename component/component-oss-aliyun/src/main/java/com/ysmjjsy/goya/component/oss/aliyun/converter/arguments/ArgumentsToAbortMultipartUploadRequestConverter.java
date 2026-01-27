package com.ysmjjsy.goya.component.oss.aliyun.converter.arguments;

import com.aliyun.oss.model.AbortMultipartUploadRequest;
import com.ysmjjsy.goya.component.oss.core.arguments.multipart.AbortMultipartUploadArguments;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>统一定义 AbortMultipartUploadArguments 转 S3 AbortMultipartUploadRequest 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 20:17
 */
public class ArgumentsToAbortMultipartUploadRequestConverter implements Converter<AbortMultipartUploadArguments, AbortMultipartUploadRequest> {
    @Override
    public AbortMultipartUploadRequest convert(AbortMultipartUploadArguments source) {
        return new AbortMultipartUploadRequest(source.getBucketName(), source.getObjectName(), source.getUploadId());
    }
}
