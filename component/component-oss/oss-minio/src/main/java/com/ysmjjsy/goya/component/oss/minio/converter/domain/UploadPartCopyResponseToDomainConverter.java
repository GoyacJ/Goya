package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.core.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.UploadPartCopyDomain;
import io.minio.UploadPartCopyResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>Minio UploadPartCopyResponse 转 UploadPartCopyDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:26
 */
public class UploadPartCopyResponseToDomainConverter implements Converter<UploadPartCopyResponse, UploadPartCopyDomain> {
    @Override
    public UploadPartCopyDomain convert(UploadPartCopyResponse source) {

        UploadPartCopyDomain domain = new UploadPartCopyDomain();
        domain.setUploadId(source.uploadId());
        domain.setPartNumber(source.partNumber());

        if (ObjectUtils.isNotEmpty(source.result())) {
            domain.setEtag(source.result().etag());
            domain.setLastModifiedDate(GoyaDateUtils.zonedDateTimeToLocalDateTime(source.result().lastModified()));
        }

        return domain;
    }
}