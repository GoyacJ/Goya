package com.ysmjjsy.goya.component.oss.core.domain.multipart;

import com.ysmjjsy.goya.component.oss.core.domain.base.BasePartDomain;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;

/**
 * <p>分片上传返回结果域对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:32
 */
@Schema(name = "分片上传返回结果域对象", title = "分片上传返回结果域对象")
public class UploadPartDomain extends BasePartDomain {

    @Serial
    private static final long serialVersionUID = 7805168834153427386L;
}