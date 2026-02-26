package com.ysmjjsy.goya.component.admin.role.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>角色菜单关系</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_role_menu")
public class IamRoleMenuEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -6304897977169236853L;

    @TableField("role_id")
    private String roleId;

    @TableField("menu_id")
    private String menuId;
}
