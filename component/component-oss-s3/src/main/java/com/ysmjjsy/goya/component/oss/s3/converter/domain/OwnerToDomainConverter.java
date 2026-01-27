package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.framework.oss.domain.base.OwnerDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.Owner;

/**
 * <p>Owner 转 OwnerDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 20:35
 */
public class OwnerToDomainConverter implements Converter<Owner, OwnerDomain> {
    
    @Override
    public OwnerDomain convert(Owner source) {
        if (source == null) {
            return null;
        }
        
        OwnerDomain domain = new OwnerDomain();
        domain.setId(source.id());
        domain.setDisplayName(source.displayName());
        return domain;
    }
}
