package com.ysmjjsy.goya.component.admin.dept.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>用户部门关系</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_user_dept")
public class IamUserDeptEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -4376719968303315612L;

    @TableField("user_id")
    private String userId;

    @TableField("dept_id")
    private String deptId;
}
