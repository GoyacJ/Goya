package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.core.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.oss.core.domain.object.ObjectMetadataDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>HeadObjectResponse 转 ObjectMetadataDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:55
 */
public class HeadObjectResponseToDomainConverter implements Converter<HeadObjectResponse, ObjectMetadataDomain> {
    
    @Override
    public ObjectMetadataDomain convert(HeadObjectResponse source) {
        ObjectMetadataDomain domain = new ObjectMetadataDomain();
        domain.setEtag(source.eTag());
        domain.setVersionId(source.versionId());
        domain.setContentLength(source.contentLength());
        domain.setContentType(source.contentType());
        
        if (source.lastModified() != null) {
            domain.setLastModified(GoyaDateUtils.toLocalDateTime(source.lastModified()));
        }
        
        if (source.metadata() != null && !source.metadata().isEmpty()) {
            Map<String, String> userMetadata = new HashMap<>(source.metadata());
            domain.setUserMetadata(userMetadata);
        }

        return domain;
    }
}
