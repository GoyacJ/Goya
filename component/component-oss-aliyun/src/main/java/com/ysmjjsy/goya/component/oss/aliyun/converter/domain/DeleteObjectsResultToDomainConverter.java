package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.DeleteObjectsResult;
import com.ysmjjsy.goya.component.framework.oss.domain.object.DeleteObjectDomain;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Aliyun DeleteObjectsResult 转 DeleteObjectDomain 转换器  </p>
 *
 * @author goya
 * @since 2023/8/12 15:30
 */
public class DeleteObjectsResultToDomainConverter implements Converter<DeleteObjectsResult, List<DeleteObjectDomain>> {
    @Override
    public List<DeleteObjectDomain> convert(DeleteObjectsResult source) {

        List<String> items = source.getDeletedObjects();

        if (CollectionUtils.isNotEmpty(items)) {
            return items.stream().map(DeleteObjectDomain::new).toList();
        }

        return new ArrayList<>();
    }
}
