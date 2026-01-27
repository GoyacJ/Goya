package com.ysmjjsy.goya.component.framework.oss.arguments.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>基础的对象版本参数定义</p>
 *
 * @author goya
 * @since 2025/11/1 14:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ObjectVersionArguments extends ObjectArguments {

    @Serial
    private static final long serialVersionUID = -6307291127142773123L;

    @Schema(name = "版本ID")
    private String versionId;
}
