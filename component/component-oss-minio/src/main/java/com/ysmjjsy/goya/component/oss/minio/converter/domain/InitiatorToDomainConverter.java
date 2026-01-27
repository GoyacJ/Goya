package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.base.OwnerDomain;
import io.minio.messages.Initiator;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Owner 转 OwnerDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:18
 */
public class InitiatorToDomainConverter implements Converter<Initiator, OwnerDomain> {

    @Override
    public OwnerDomain convert(Initiator source) {

        OwnerDomain attribute = new OwnerDomain();
        attribute.setId(source.id());
        attribute.setDisplayName(source.displayName());
        return attribute;
    }
}