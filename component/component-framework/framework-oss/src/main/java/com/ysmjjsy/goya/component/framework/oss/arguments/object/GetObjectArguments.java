package com.ysmjjsy.goya.component.framework.oss.arguments.object;

import com.ysmjjsy.goya.component.oss.core.arguments.base.ObjectConditionalReadArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>下载对象请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:50
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetObjectArguments extends ObjectConditionalReadArguments {

    @Serial
    private static final long serialVersionUID = 7521481506178361994L;
}