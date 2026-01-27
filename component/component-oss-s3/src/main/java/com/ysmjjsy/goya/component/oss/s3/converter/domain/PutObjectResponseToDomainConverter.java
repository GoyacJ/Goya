package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.object.PutObjectDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * <p>PutObjectResponse 转 PutObjectDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 22:15
 */
public class PutObjectResponseToDomainConverter implements Converter<PutObjectResponse, PutObjectDomain> {
    
    @Override
    public PutObjectDomain convert(PutObjectResponse source) {
        PutObjectDomain domain = new PutObjectDomain();
        domain.setEtag(source.eTag());
        domain.setVersionId(source.versionId());
        return domain;
    }
}
