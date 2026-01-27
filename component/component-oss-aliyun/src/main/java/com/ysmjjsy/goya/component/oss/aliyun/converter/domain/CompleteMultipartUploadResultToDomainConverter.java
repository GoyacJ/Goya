package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.CompleteMultipartUploadResult;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.CompleteMultipartUploadDomain;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>CompleteMultipartUploadResult 转 CompleteMultipartUploadDomain 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 20:58
 */
public class CompleteMultipartUploadResultToDomainConverter implements Converter<CompleteMultipartUploadResult, CompleteMultipartUploadDomain> {
    @Override
    public CompleteMultipartUploadDomain convert(CompleteMultipartUploadResult source) {

        CompleteMultipartUploadDomain domain = new CompleteMultipartUploadDomain();
        domain.setEtag(source.getETag());
        domain.setVersionId(source.getVersionId());
        domain.setBucketName(source.getBucketName());
        domain.setObjectName(source.getKey());

        return domain;
    }
}
