package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.Owner;
import com.ysmjjsy.goya.component.oss.core.domain.base.OwnerDomain;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Owner 转 OwnerDomain 转换器 </p>
 *
 * @author goya
 * @since 2023/8/13 23:09
 */
public class OwnerToDomainConverter implements Converter<Owner, OwnerDomain> {

    @Override
    public OwnerDomain convert(Owner source) {
        OwnerDomain attribute = new OwnerDomain();
        attribute.setId(source.getId());
        attribute.setDisplayName(source.getDisplayName());
        return attribute;
    }
}
