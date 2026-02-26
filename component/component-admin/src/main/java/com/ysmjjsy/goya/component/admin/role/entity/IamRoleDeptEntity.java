package com.ysmjjsy.goya.component.admin.role.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>角色部门关系</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_role_dept")
public class IamRoleDeptEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 9065433803555091383L;

    @TableField("role_id")
    private String roleId;

    @TableField("dept_id")
    private String deptId;
}
