package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.multipart.UploadPartDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

/**
 * <p>UploadPartResponse 转 UploadPartDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:07
 */
public class UploadPartResponseToDomainConverter implements Converter<UploadPartResponse, UploadPartDomain> {
    
    @Override
    public UploadPartDomain convert(UploadPartResponse source) {
        UploadPartDomain domain = new UploadPartDomain();
        domain.setEtag(source.eTag());
        return domain;
    }
}
