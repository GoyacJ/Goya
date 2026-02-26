package com.ysmjjsy.goya.component.admin.role.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>用户角色关系</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_user_role")
public class IamUserRoleEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -3752643350910791830L;

    @TableField("user_id")
    private String userId;

    @TableField("role_id")
    private String roleId;
}
