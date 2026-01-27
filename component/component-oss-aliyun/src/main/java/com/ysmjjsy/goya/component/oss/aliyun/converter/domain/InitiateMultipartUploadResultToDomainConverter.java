package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.InitiateMultipartUploadDomain;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>InitiateMultipartUploadResult 转 InitiateMultipartUploadDomain 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 21:09
 */
public class InitiateMultipartUploadResultToDomainConverter implements Converter<InitiateMultipartUploadResult, InitiateMultipartUploadDomain> {
    @Override
    public InitiateMultipartUploadDomain convert(InitiateMultipartUploadResult source) {

        InitiateMultipartUploadDomain domain = new InitiateMultipartUploadDomain();
        domain.setUploadId(source.getUploadId());
        domain.setBucketName(source.getBucketName());
        domain.setObjectName(source.getKey());
        return domain;
    }
}
