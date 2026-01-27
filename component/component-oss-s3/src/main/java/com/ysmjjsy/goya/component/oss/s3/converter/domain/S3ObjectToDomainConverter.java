package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.framework.oss.domain.object.ObjectDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * <p>S3Object 转 ObjectDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:18
 */
public class S3ObjectToDomainConverter implements Converter<S3Object, ObjectDomain> {
    
    @Override
    public ObjectDomain convert(S3Object source) {
        ObjectDomain domain = new ObjectDomain();
        domain.setObjectName(source.key());
        domain.setETag(source.eTag());
        domain.setSize(source.size());
        
        if (source.lastModified() != null) {
            domain.setLastModified(GoyaDateUtils.toLocalDateTime(source.lastModified()));
        }
        
        if (source.storageClass() != null) {
            domain.setStorageClass(source.storageClass().toString());
        }
        
        domain.setIsDir(false);
        
        return domain;
    }
}
