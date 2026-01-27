package com.ysmjjsy.goya.component.oss.s3.converter.domain;

import com.ysmjjsy.goya.component.framework.oss.domain.multipart.ListMultipartUploadsDomain;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.UploadDomain;
import com.ysmjjsy.goya.component.framework.oss.utils.OssConverterUtils;
import org.springframework.core.convert.converter.Converter;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListMultipartUploadsResponse;
import software.amazon.awssdk.services.s3.model.MultipartUpload;

/**
 * <p>ListMultipartUploadsResponse 转 ListMultipartUploadsDomain 转换器</p>
 *
 * @author goya
 * @since 2023/8/14 20:32
 */
public class ListMultipartUploadsResponseToDomainConverter implements Converter<ListMultipartUploadsResponse, ListMultipartUploadsDomain> {

    private final Converter<MultipartUpload, UploadDomain> uploadConverter = new MultipartUploadToDomainConverter();

    @Override
    public ListMultipartUploadsDomain convert(ListMultipartUploadsResponse source) {
        ListMultipartUploadsDomain domain = new ListMultipartUploadsDomain();
        domain.setBucketName(source.bucket());
        domain.setDelimiter(source.delimiter());
        domain.setPrefix(source.prefix());
        domain.setMaxUploads(source.maxUploads());
        domain.setKeyMarker(source.keyMarker());
        domain.setUploadIdMarker(source.uploadIdMarker());
        
        if (source.encodingType() != null) {
            domain.setEncodingType(source.encodingType().toString());
        }

        domain.setTruncated(source.isTruncated());
        domain.setNextKeyMarker(source.nextKeyMarker());
        domain.setNextUploadIdMarker(source.nextUploadIdMarker());
        
        if (source.commonPrefixes() != null) {
            domain.setCommonPrefixes(source.commonPrefixes().stream()
                    .map(CommonPrefix::prefix)
                    .toList());
        }

        if (source.uploads() != null) {
            domain.setMultipartUploads(OssConverterUtils.toDomains(source.uploads(), uploadConverter));
        }

        return domain;
    }
}
