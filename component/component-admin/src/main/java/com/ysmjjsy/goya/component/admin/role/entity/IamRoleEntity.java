package com.ysmjjsy.goya.component.admin.role.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>角色实体</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_role")
public class IamRoleEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 8289053183914038576L;

    @TableField("role_code")
    private String roleCode;

    @TableField("role_name")
    private String roleName;

    @TableField("data_scope")
    private String dataScope;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;
}
