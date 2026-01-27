package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.MultipartUpload;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.UploadDomain;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>List<MultipartUpload> 转 List<UploadDomain> 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 20:35
 */
public class MultipartUploadToDomainConverter implements Converter<List<MultipartUpload>, List<UploadDomain>> {

    @Override
    public List<UploadDomain> convert(List<MultipartUpload> source) {

        if (CollectionUtils.isNotEmpty(source)) {
            return source.stream().map(this::convert).toList();
        }

        return new ArrayList<>();
    }

    private UploadDomain convert(MultipartUpload source) {
        UploadDomain domain = new UploadDomain();
        domain.setKey(source.getKey());
        domain.setUploadId(source.getUploadId());
        domain.setStorageClass(source.getStorageClass());
        domain.setInitiated(DateUtils.toLocalDateTime(source.getInitiated()));
        return domain;
    }
}
