package com.ysmjjsy.goya.component.mybatisplus.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.framework.security.domain.ResourceType;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>数据资源表实体。</p>
 *
 * @author goya
 * @since 2026/1/31 11:20
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_resource")
public class DataResourceEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 5745049171500835839L;

    /**
     * 租户编码。
     */
    @TableField("tenant_code")
    private String tenantCode;

    /**
     * 资源全局唯一 hashcode。
     */
    @TableField("resource_hashcode")
    private String resourceHashcode;

    /**
     * 资源编码（租户内唯一）。
     */
    @TableField("resource_code")
    private String resourceCode;

    /**
     * 父资源编码。
     */
    @TableField("resource_parent_code")
    private String resourceParentCode;

    /**
     * 父资源编码集合（逗号分隔）。
     */
    @TableField("resource_parent_codes")
    private String resourceParentCodes;

    /**
     * 资源操作类型（JSON 或结构化字符串）。
     */
    @TableField("resource_oper_type")
    private String resourceOperType;

    /**
     * 资源名称。
     */
    @TableField("resource_name")
    private String resourceName;

    /**
     * 资源类型（如 DB/TABLE/FIELD/ROW）。
     */
    @TableField("resource_type")
    private ResourceType resourceType;

    /**
     * 资源描述。
     */
    @TableField("resource_desc")
    private String resourceDesc;

    /**
     * 资源负责人。
     */
    @TableField("resource_owner")
    private String resourceOwner;
}
