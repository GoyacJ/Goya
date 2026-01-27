package com.ysmjjsy.goya.component.framework.oss.domain.base;

import com.ysmjjsy.goya.component.framework.common.pojo.IEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

/**
 * <p>所有者基础属性实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:26
 */
@Data
public class OwnerDomain implements IEntity {

    @Serial
    private static final long serialVersionUID = -9170554266742219079L;

    /**
     * 所有者 ID
     */
    @Schema(name = "所有者 ID")
    private String id;

    /**
     * 所有者显示名称
     */
    @Schema(name = "所有者显示名称")
    private String displayName;
}
