package com.ysmjjsy.goya.component.framework.oss.arguments.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>基础 Object Read 参数实体</p>
 * 创建继承关系，等后续操作需要补充参数时，再行补充。
 * @author goya
 * @since 2025/11/1 14:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ObjectReadArguments extends ObjectVersionArguments {
    @Serial
    private static final long serialVersionUID = 2460456743406787855L;

}
