package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.PartSummaryDomain;
import io.minio.messages.Part;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>基础的统一定义请求属性转换为 Minio Parts 参数转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:21
 */
public class PartToDomainConverter implements Converter<List<Part>, List<PartSummaryDomain>> {

    @Override
    public List<PartSummaryDomain> convert(List<Part> source) {
        if (CollectionUtils.isNotEmpty(source)) {
            return source.stream().map(this::convert).toList();
        }
        return new ArrayList<>();
    }

    private PartSummaryDomain convert(Part source) {

        PartSummaryDomain domain = new PartSummaryDomain();
        domain.setPartSize(source.partSize());
        domain.setLastModifiedDate(GoyaDateUtils.zonedDateTimeToLocalDateTime(source.lastModified()));
        domain.setPartNumber(source.partNumber());
        domain.setEtag(source.etag());
        return domain;
    }
}

