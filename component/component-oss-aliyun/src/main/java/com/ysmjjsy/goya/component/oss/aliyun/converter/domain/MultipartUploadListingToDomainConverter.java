package com.ysmjjsy.goya.component.oss.aliyun.converter.domain;

import com.aliyun.oss.model.MultipartUpload;
import com.aliyun.oss.model.MultipartUploadListing;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.ListMultipartUploadsDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.UploadDomain;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * <p>MultipartUploadListing 转 ListMultipartUploadsDomain 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 20:32
 */
public class MultipartUploadListingToDomainConverter implements Converter<MultipartUploadListing, ListMultipartUploadsDomain> {

    private final Converter<List<MultipartUpload>, List<UploadDomain>> toDomain = new MultipartUploadToDomainConverter();

    @Override
    public ListMultipartUploadsDomain convert(MultipartUploadListing source) {

        ListMultipartUploadsDomain domain = new ListMultipartUploadsDomain();
        domain.setTruncated(source.isTruncated());
        domain.setNextKeyMarker(source.getNextKeyMarker());
        domain.setNextUploadIdMarker(source.getNextUploadIdMarker());
        domain.setMultipartUploads(toDomain.convert(source.getMultipartUploads()));
        domain.setCommonPrefixes(source.getCommonPrefixes());
        domain.setDelimiter(source.getDelimiter());
        domain.setPrefix(source.getPrefix());
        domain.setMaxUploads(source.getMaxUploads());
        domain.setKeyMarker(source.getKeyMarker());
        domain.setUploadIdMarker(source.getUploadIdMarker());
        domain.setBucketName(source.getBucketName());
        return domain;
    }
}
