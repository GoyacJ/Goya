package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaDateUtils;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.UploadPartCopyDomain;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.UploadPartCopyResponse;

/**
 * <p>UploadPartCopyResponse 转 UploadPartCopyDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 21:02
 */
public class CopyObjectResponseToDomainConverter implements Converter<UploadPartCopyResponse, UploadPartCopyDomain> {

    @Override
    public UploadPartCopyDomain convert(UploadPartCopyResponse source) {
        UploadPartCopyDomain domain = new UploadPartCopyDomain();
        
        if (source.copyPartResult() != null) {
            domain.setEtag(source.copyPartResult().eTag());
            if (source.copyPartResult().lastModified() != null) {
                domain.setLastModifiedDate(GoyaDateUtils.toLocalDateTime(source.copyPartResult().lastModified()));
            }
        }
        
        return domain;
    }
}
