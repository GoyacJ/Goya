package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.domain.multipart.PartSummaryDomain;
import io.minio.messages.Part;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * <p>基础的统一定义请求属性转换为 Minio Parts 参数转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:17
 */
public class DomainToPartConverter implements Converter<List<PartSummaryDomain>, Part[]> {
    @Override
    public Part[] convert(List<PartSummaryDomain> source) {
        if (CollectionUtils.isNotEmpty(source)) {
            List<Part> parts = source.stream().map(item -> new Part(item.getPartNumber(), item.getEtag())).toList();
            Part[] result = new Part[parts.size()];
            return parts.toArray(result);
        }
        return new Part[]{};
    }
}