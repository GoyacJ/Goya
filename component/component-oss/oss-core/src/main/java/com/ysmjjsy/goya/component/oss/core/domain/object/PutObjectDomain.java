package com.ysmjjsy.goya.component.oss.core.domain.object;

import com.ysmjjsy.goya.component.oss.core.domain.base.ObjectWriteDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>放置对象返回结果域对象</p>
 *
 * @author goya
 * @since 2025/11/1 14:34
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PutObjectDomain extends ObjectWriteDomain {

    @Serial
    private static final long serialVersionUID = -5754237108066257313L;
}