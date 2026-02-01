package com.ysmjjsy.goya.component.mybatisplus.definition;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.ysmjjsy.goya.component.mybatisplus.constants.MybatisPlusConst;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/28 23:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BaseEntity extends AuditEntity {

    @Serial
    private static final long serialVersionUID = 6324816514595393430L;

    /**
     * 主键 ID。
     * <p>
     * 仅用于数据库内部标识，不参与任何业务语义。
     */
    @TableId(value = MybatisPlusConst.FIELD_ID, type = IdType.AUTO)
    protected String id;

    /**
     * 租户 ID（主键）。
     */
    @TableField(MybatisPlusConst.FIELD_TENANT_ID)
    private String tenantId;


}
