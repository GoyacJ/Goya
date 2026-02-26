package com.ysmjjsy.goya.component.admin.menu.entity;

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
 * <p>菜单实体</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_menu")
public class IamMenuEntity extends BaseEntity implements AdminTreeSupport.TreeNode<IamMenuEntity> {

    @Serial
    private static final long serialVersionUID = -1260305737952003353L;

    @TableField("menu_code")
    private String menuCode;

    @TableField("menu_name")
    private String menuName;

    @TableField("menu_type")
    private String menuType;

    @TableField("path")
    private String path;

    @TableField("component")
    private String component;

    @TableField("permission_code")
    private String permissionCode;

    @TableField("icon")
    private String icon;

    @TableField("parent_id")
    private String parentId;

    @TableField("parent_ids")
    private String parentIds;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private String status;

    @TableField("hidden")
    private Boolean hidden;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private List<IamMenuEntity> children = new ArrayList<>();

    @Override
    public String nodeId() {
        return getId();
    }

    @Override
    public String parentId() {
        return getParentId();
    }

    @Override
    public List<IamMenuEntity> children() {
        return children;
    }
}
