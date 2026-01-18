package com.ysmjjsy.goya.component.oss.business;

import com.ysmjjsy.goya.component.oss.definition.OssBusiness;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>创建分片上传返回结果业务对象</p>
 *
 * @author goya
 * @since 2025/11/3 09:34
 */
@Data
@Schema(name = "创建分片上传返回结果业务对象", title = "创建分片上传返回结果业务对象")
public class CreateMultipartUploadBusiness implements OssBusiness {

    @Serial
    private static final long serialVersionUID = -2363944692768040910L;

    @Schema(name = "上传ID")
    private String uploadId;

    @Schema(name = "分片上传URL", description = "分片上传所有分片对相应的预签名地址")
    private List<String> uploadUrls;

    public CreateMultipartUploadBusiness(String uploadId) {
        this.uploadId = uploadId;
        this.uploadUrls = new ArrayList<>();
    }

    public void append(String uploadUrl) {
        uploadUrls.add(uploadUrls.size(), uploadUrl);
    }
}
