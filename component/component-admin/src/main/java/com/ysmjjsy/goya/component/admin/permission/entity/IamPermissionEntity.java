package com.ysmjjsy.goya.component.admin.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>权限实体</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_permission")
public class IamPermissionEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -8191372935684722728L;

    @TableField("permission_code")
    private String permissionCode;

    @TableField("permission_name")
    private String permissionName;

    @TableField("resource_type")
    private String resourceType;

    @TableField("resource_code")
    private String resourceCode;

    @TableField("action_code")
    private String actionCode;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;
}
