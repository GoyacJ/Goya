package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.PartSummary;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.PartSummaryDomain;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>List<PartSummary> 转 List<PartSummaryDomain> 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 20:47
 */
public class PartSummaryToDomainConverter implements Converter<List<PartSummary>, List<PartSummaryDomain>> {

    @Override
    public List<PartSummaryDomain> convert(List<PartSummary> source) {
        if (CollectionUtils.isNotEmpty(source)) {
            return source.stream().map(this::convert).toList();
        }
        return new ArrayList<>();
    }

    private PartSummaryDomain convert(PartSummary source) {
        PartSummaryDomain domain = new PartSummaryDomain();
        domain.setPartSize(source.getSize());
        domain.setLastModifiedDate(DateUtils.toLocalDateTime(source.getLastModified()));
        domain.setPartNumber(source.getPartNumber());
        domain.setEtag(source.getETag());
        return domain;
    }
}
