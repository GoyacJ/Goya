package com.ysmjjsy.goya.component.oss.minio.converter.domain;

import com.ysmjjsy.goya.component.oss.core.arguments.multipart.ListMultipartUploadsArguments;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.ListMultipartUploadsDomain;
import com.ysmjjsy.goya.component.oss.core.domain.multipart.UploadDomain;
import io.minio.messages.ListMultipartUploadsResult;
import io.minio.messages.Upload;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * <p>ListMultipartUploadsResult 转 ListMultipartUploadsDomain 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:19
 */
public class ListMultipartUploadsResultToDomainConverter implements Converter<ListMultipartUploadsResult, ListMultipartUploadsDomain> {

    private final ListMultipartUploadsArguments listMultipartUploadsArguments;
    private final Converter<List<Upload>, List<UploadDomain>> toMultipartUpload = new UploadToDomainConverter();

    public ListMultipartUploadsResultToDomainConverter(ListMultipartUploadsArguments listMultipartUploadsArguments) {
        this.listMultipartUploadsArguments = listMultipartUploadsArguments;
    }

    @Override
    public ListMultipartUploadsDomain convert(ListMultipartUploadsResult source) {
        ListMultipartUploadsDomain domain = new ListMultipartUploadsDomain();
        domain.setTruncated(source.isTruncated());
        domain.setNextKeyMarker(source.nextKeyMarker());
        domain.setNextUploadIdMarker(source.nextUploadIdMarker());
        domain.setMultipartUploads(toMultipartUpload.convert(source.uploads()));
        domain.setDelimiter(listMultipartUploadsArguments.getDelimiter());
        domain.setPrefix(listMultipartUploadsArguments.getPrefix());
        domain.setMaxUploads(source.maxUploads());
        domain.setKeyMarker(source.keyMarker());
        domain.setUploadIdMarker(source.uploadIdMarker());
        domain.setEncodingType(source.encodingType());
        domain.setBucketName(source.bucketName());
        return domain;
    }
}
