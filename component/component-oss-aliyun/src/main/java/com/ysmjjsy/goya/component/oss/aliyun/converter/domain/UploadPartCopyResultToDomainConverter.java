package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.UploadPartCopyResult;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.UploadPartCopyDomain;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>CopyPartResult 转 UploadPartCopyDomain 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 21:02
 */
public class UploadPartCopyResultToDomainConverter implements Converter<UploadPartCopyResult, UploadPartCopyDomain> {

    @Override
    public UploadPartCopyDomain convert(UploadPartCopyResult source) {

        UploadPartCopyDomain domain = new UploadPartCopyDomain();
        domain.setPartNumber(source.getPartNumber());
        domain.setEtag(source.getETag());

        return domain;
    }
}
