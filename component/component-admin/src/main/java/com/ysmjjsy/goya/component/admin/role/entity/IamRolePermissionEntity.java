package com.ysmjjsy.goya.component.admin.role.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>角色权限关系</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_role_permission")
public class IamRolePermissionEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -1667061685533804210L;

    @TableField("role_id")
    private String roleId;

    @TableField("permission_id")
    private String permissionId;
}
