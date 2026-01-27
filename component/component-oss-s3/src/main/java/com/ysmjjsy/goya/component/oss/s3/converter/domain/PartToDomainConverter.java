package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.PartSummaryDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.Part;

/**
 * <p>Part 转 PartSummaryDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 20:40
 */
public class PartToDomainConverter implements Converter<Part, PartSummaryDomain> {
    
    @Override
    public PartSummaryDomain convert(Part source) {
        PartSummaryDomain domain = new PartSummaryDomain();
        domain.setPartNumber(source.partNumber());
        domain.setEtag(source.eTag());
        
        if (source.size() != null) {
            domain.setPartSize(source.size());
        }
        
        if (source.lastModified() != null) {
            domain.setLastModifiedDate(GoyaDateUtils.toLocalDateTime(source.lastModified()));
        }
        
        return domain;
    }
}
