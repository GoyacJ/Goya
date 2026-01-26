package com.ysmjjsy.goya.component.oss.core.domain.object;

import com.ysmjjsy.goya.component.oss.core.domain.base.BaseDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.InputStream;
import java.io.Serial;

/**
 * <p>获取对象返回结果域对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetObjectDomain extends BaseDomain {

    @Serial
    private static final long serialVersionUID = -5780388557015565586L;

    private InputStream objectContent;
}
