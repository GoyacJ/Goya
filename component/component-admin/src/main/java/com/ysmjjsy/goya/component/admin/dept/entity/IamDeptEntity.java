package com.ysmjjsy.goya.component.admin.dept.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ysmjjsy.goya.component.admin.support.AdminTreeSupport;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>部门实体</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_dept")
public class IamDeptEntity extends BaseEntity implements AdminTreeSupport.TreeNode<IamDeptEntity> {

    @Serial
    private static final long serialVersionUID = 8971266349368324099L;

    @TableField("dept_code")
    private String deptCode;

    @TableField("dept_name")
    private String deptName;

    @TableField("parent_id")
    private String parentId;

    @TableField("parent_ids")
    private String parentIds;

    @TableField("leader")
    private String leader;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private String status;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private List<IamDeptEntity> children = new ArrayList<>();

    @Override
    public String nodeId() {
        return getId();
    }

    @Override
    public String parentId() {
        return getParentId();
    }

    @Override
    public List<IamDeptEntity> children() {
        return children;
    }
}
