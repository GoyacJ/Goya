package com.ysmjjsy.goya.component.oss.core.arguments.object;

import com.ysmjjsy.goya.component.oss.core.arguments.base.ObjectConditionalReadArguments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>获取对象元数据请求参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetObjectMetadataArguments extends ObjectConditionalReadArguments {
    @Serial
    private static final long serialVersionUID = 1885413254822305194L;

}

