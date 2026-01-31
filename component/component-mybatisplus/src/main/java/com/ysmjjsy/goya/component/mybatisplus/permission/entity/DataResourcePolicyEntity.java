package com.ysmjjsy.goya.component.mybatisplus.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.framework.security.domain.PolicyEffect;
import com.ysmjjsy.goya.component.framework.security.domain.PolicyScope;
import com.ysmjjsy.goya.component.framework.security.domain.ResourceRange;
import com.ysmjjsy.goya.component.framework.security.domain.ResourceType;
import com.ysmjjsy.goya.component.framework.security.domain.SubjectType;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>资源策略表实体。</p>
 *
 * <p>用于存储策略核心字段（主体、资源、操作、效果、范围、DSL）。</p>
 *
 * @author goya
 * @since 2026/1/31 11:20
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_resource_policy")
public class DataResourcePolicyEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -5354462004242664495L;

    /**
     * 租户编码。
     */
    @TableField("tenant_code")
    private String tenantCode;

    /**
     * 主体类型（USER/ROLE/TEAM/ORG 或数值编码）。
     */
    @TableField("subject_type")
    private SubjectType subjectType;

    /**
     * 主体 ID。
     */
    @TableField("subject_id")
    private String subjectId;

    /**
     * 资源类型（DB/TABLE/FIELD/ROW/API/FILE/MENU/CUSTOM）。
     */
    @TableField("resource_type")
    private ResourceType resourceType;

    /**
     * 资源编码。
     */
    @TableField("resource_code")
    private String resourceCode;

    /**
     * 操作编码（QUERY/CREATE/UPDATE/DELETE 等）。
     */
    @TableField("action")
    private String actionCode;

    /**
     * 策略效果（ALLOW/DENY）。
     */
    @TableField("policy_effect")
    private PolicyEffect policyEffect;

    /**
     * 策略范围（RESOURCE/ROW/COLUMN）。
     */
    @TableField("policy_scope")
    private PolicyScope policyScope;

    /**
     * 行级 DSL 表达式。
     */
    @TableField("range_dsl")
    private String rangeDsl;

    /**
     * 列级允许名单（逗号分隔）。
     */
    @TableField("allow_columns")
    private String allowColumns;

    /**
     * 列级拒绝名单（逗号分隔）。
     */
    @TableField("deny_columns")
    private String denyColumns;

    /**
     * 是否继承到子资源。
     */
    @TableField("inherit_flag")
    private Boolean inheritFlag;

    /**
     * 资源范围（SELF/CHILDREN/SELF_AND_CHILDREN）。
     */
    @TableField("resource_range")
    private ResourceRange resourceRange;

    /**
     * 是否永不过期。
     */
    @TableField("never_expire")
    private Boolean neverExpire;

    /**
     * 过期时间。
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
}
