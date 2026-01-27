package com.ysmjjsy.goya.component.framework.oss.domain.object;

import com.ysmjjsy.goya.component.oss.core.domain.base.ObjectWriteDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>上传对象返回结果域对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UploadObjectDomain extends ObjectWriteDomain {
    @Serial
    private static final long serialVersionUID = 2276957675518193261L;
}
