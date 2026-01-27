package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.UploadPartResult;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.UploadPartDomain;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>UploadPartResult 转 UploadPartDomain 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 21:07
 */
public class UploadPartResultToDomainConverter implements Converter<UploadPartResult, UploadPartDomain> {
    @Override
    public UploadPartDomain convert(UploadPartResult source) {

        UploadPartDomain domain = new UploadPartDomain();
        domain.setPartNumber(source.getPartNumber());
        domain.setEtag(source.getETag());

        return domain;
    }
}
