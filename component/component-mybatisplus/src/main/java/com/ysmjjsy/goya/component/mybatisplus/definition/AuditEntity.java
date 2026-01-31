package com.ysmjjsy.goya.component.mybatisplus.definition;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import com.ysmjjsy.goya.component.framework.common.pojo.IEntity;
import com.ysmjjsy.goya.component.mybatisplus.constants.MybatisPlusConst;
import lombok.Data;

import java.io.Serial;
import java.time.Instant;

/**
 * <p>审计抽象定义</p>
 *
 * @author goya
 * @since 2026/1/29 21:47
 */
@Data
public abstract class AuditEntity implements IEntity {

    @Serial
    private static final long serialVersionUID = -9141699225413993965L;

    /**
     * 删除标志。
     * <p>
     * 由框架统一填充，用于审计。
     */
    @TableLogic
    @TableField(value = MybatisPlusConst.FIELD_DEL_FLAG, fill = FieldFill.INSERT)
    protected Boolean delFlag = false;

    /**
     * 乐观锁
     */
    @Version
    @TableField(value = MybatisPlusConst.FIELD_VERSION, fill = FieldFill.INSERT)
    protected Integer version = 0;

    /**
     * 创建时间。
     * <p>
     * 由框架统一填充，用于审计。
     */
    @TableField(value = MybatisPlusConst.FIELD_CREATED_AT, fill = FieldFill.INSERT)
    protected Instant createdAt;

    /**
     * 创建人。
     * <p>
     * 通常为管理员账号或治理系统操作人。
     */
    @TableField(value = MybatisPlusConst.FIELD_CREATED_BY, fill = FieldFill.INSERT)
    protected String createdBy;

    /**
     * 更新时间。
     * <p>
     * 每次规则内容或启用状态发生变化都会更新。
     */
    @TableField(value = MybatisPlusConst.FIELD_UPDATED_AT, fill = FieldFill.INSERT_UPDATE)
    protected Instant updatedAt;

    /**
     * 更新人。
     * <p>
     * 用于审计“是谁修改了权限规则”。
     */
    @TableField(value = MybatisPlusConst.FIELD_UPDATED_BY, fill = FieldFill.INSERT_UPDATE)
    protected String updatedBy;
}
