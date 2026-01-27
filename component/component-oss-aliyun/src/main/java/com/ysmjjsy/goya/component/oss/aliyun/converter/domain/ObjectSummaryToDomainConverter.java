package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.OSSObjectSummary;
import com.ysmjjsy.goya.component.oss.core.domain.base.OwnerDomain;
import com.ysmjjsy.goya.component.oss.core.domain.object.ObjectDomain;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Aliyun ObjectSummary 转 ObjectDomain 转换器 </p>
 *
 * @author goya
 * @since 2023/8/10 16:05
 */
public class ObjectSummaryToDomainConverter implements Converter<OSSObjectSummary, ObjectDomain> {

    private final String delimiter;

    public ObjectSummaryToDomainConverter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public ObjectDomain convert(OSSObjectSummary source) {

        ObjectDomain objectDomain = new ObjectDomain();
        objectDomain.setBucketName(source.getBucketName());
        objectDomain.setObjectName(source.getKey());
        objectDomain.setETag(source.getETag());
        objectDomain.setSize(source.getSize());
        objectDomain.setLastModified(DateUtils.toLocalDateTime(source.getLastModified()));
        objectDomain.setStorageClass(source.getStorageClass());

        if (ObjectUtils.isNotEmpty(source.getOwner())) {
            OwnerDomain ownerAttributeDomain = new OwnerDomain();
            ownerAttributeDomain.setId(ownerAttributeDomain.getId());
            ownerAttributeDomain.setDisplayName(ownerAttributeDomain.getDisplayName());
            objectDomain.setOwnerAttribute(ownerAttributeDomain);
        }

        objectDomain.setIsDir(StringUtils.isNotBlank(this.delimiter) && Strings.CS.contains(source.getKey(), this.delimiter));

        return objectDomain;
    }
}
