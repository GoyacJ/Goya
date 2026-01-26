package com.ysmjjsy.goya.component.oss.core.domain.multipart;

import com.ysmjjsy.goya.component.oss.core.arguments.multipart.ListMultipartUploadsArguments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>分片上传列表返回结果域对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "分片上传列表返回结果域对象", title = "分片上传列表返回结果域对象")
public class ListMultipartUploadsDomain extends ListMultipartUploadsArguments {

    @Serial
    private static final long serialVersionUID = 7535478725872222401L;
    
    private boolean isTruncated;

    private String nextKeyMarker;

    private String nextUploadIdMarker;

    private List<UploadDomain> multipartUploads = new ArrayList<>();

    private List<String> commonPrefixes = new ArrayList<>();
}
