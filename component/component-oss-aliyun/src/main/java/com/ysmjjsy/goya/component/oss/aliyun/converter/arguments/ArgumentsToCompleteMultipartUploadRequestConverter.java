package com.ysmjjsy.goya.component.oss.aliyun.converter.arguments;

import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.PartETag;
import com.ysmjjsy.goya.component.framework.oss.arguments.multipart.CompleteMultipartUploadArguments;
import com.ysmjjsy.goya.component.framework.oss.domain.multipart.PartSummaryDomain;
import com.ysmjjsy.goya.component.oss.aliyun.definition.arguments.ArgumentsToBucketConverter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>统一定义 CompleteMultipartUploadArguments 转 S3 CompleteMultipartUploadRequest 转换器 </p>
 *
 * @author goya
 * @since 2023/8/14 17:58
 */
public class ArgumentsToCompleteMultipartUploadRequestConverter extends ArgumentsToBucketConverter<CompleteMultipartUploadArguments, CompleteMultipartUploadRequest> {
    @Override
    public CompleteMultipartUploadRequest getInstance(CompleteMultipartUploadArguments arguments) {
        return new CompleteMultipartUploadRequest(arguments.getBucketName(), arguments.getObjectName(), arguments.getUploadId(), convert(arguments.getParts()));
    }

    private List<PartETag> convert(List<PartSummaryDomain> attributes) {
        if (CollectionUtils.isNotEmpty(attributes)) {
            return attributes.stream().map(item -> new PartETag(item.getPartNumber(), item.getEtag())).toList();
        }
        return new ArrayList<>();
    }
}
