package com.ysmjjsy.goya.component.mybatisplus.permission.rule;

import com.baomidou.mybatisplus.annotation.*;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.Instant;

/**
 * <p>权限规则集实体（按 subject + resource 存储）</p>
 * <p>
 * 该实体用于描述：
 * <ul>
 *   <li>某个租户（tenantId）</li>
 *   <li>某个授权主体（subjectId，如用户/角色/组合主体）</li>
 *   <li>在某个逻辑资源（resource，如 ORDER / CUSTOMER）</li>
 * </ul>
 * 下所拥有的数据访问规则。
 *
 * <h2>设计说明</h2>
 * <ul>
 *   <li>一条记录表示一个 subject 在一个 resource 上的完整规则集</li>
 *   <li>规则以 JSON 形式存储（ruleJson），必须是结构化规则，禁止 raw SQL</li>
 *   <li>运行时由 {@code PermissionRuleStore} 加载并反序列化为 RuleSet</li>
 *   <li>规则是否生效由 enabled 控制，而不是删除记录</li>
 * </ul>
 *
 * <h2>与版本机制的关系</h2>
 * <p>
 * 本表<strong>不直接维护版本号</strong>。
 * 每一次对规则集的新增 / 更新 / 删除，
 * 都必须同时递增 {@code permission_subject_version} 表中的 subject 版本号，
 * 用于驱动权限缓存的快速失效与生效。
 * @author goya
 * @since 2026/1/29 00:17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("permission_rule_set")
public class PermissionRuleSetEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 2199723856089384298L;

    /**
     * 主键 ID。
     * <p>
     * 仅用于数据库内部标识，不参与任何业务语义。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户 ID。
     * <p>
     * 用于多租户隔离，不同租户之间的权限规则完全隔离。
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 授权主体 ID。
     * <p>
     * 用于标识“谁在访问数据”，可以是：
     * <ul>
     *   <li>userId</li>
     *   <li>roleId</li>
     *   <li>组合主体（如 userId:roleId）</li>
     * </ul>
     *
     * <p>
     * subjectId 是权限规则加载与缓存的核心维度之一。
     */
    @TableField("subject_id")
    private String subjectId;

    /**
     * 逻辑资源标识。
     * <p>
     * 表示“访问的是什么资源”，例如：
     * <ul>
     *   <li>ORDER</li>
     *   <li>CUSTOMER</li>
     *   <li>PROJECT</li>
     * </ul>
     *
     * <p>
     * resource 并非数据库表名，而是通过 {@code ResourceRegistry}
     * 映射到一个或多个具体表。
     */
    @TableField("resource")
    private String resource;

    /**
     * 规则集 JSON。
     * <p>
     * 存储结构化的权限规则定义，典型内容包括：
     * <ul>
     *   <li>规则列表（rules）</li>
     *   <li>谓词定义（EQ / IN / BETWEEN / LIKE 等）</li>
     *   <li>变量引用（${userId} / ${deptIds} 等）</li>
     * </ul>
     *
     * <p>
     * 该字段<strong>禁止存储 raw SQL</strong>，
     * 只能由治理端通过结构化规则生成。
     *
     * <p>
     * 运行时会通过 {@code PermissionRuleJsonCodec}
     * 反序列化为 {@code RuleSet} 对象。
     */
    @TableField("rule_json")
    private String ruleJson;

    /**
     * 是否启用该规则集。
     * <p>
     * false 表示该规则被暂时禁用：
     * <ul>
     *   <li>记录仍然存在（便于审计与回滚）</li>
     *   <li>运行时 {@code PermissionRuleStore} 将忽略该规则</li>
     * </ul>
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 创建时间。
     * <p>
     * 由框架统一填充，用于审计。
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    /**
     * 创建人。
     * <p>
     * 通常为管理员账号或治理系统操作人。
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新时间。
     * <p>
     * 每次规则内容或启用状态发生变化都会更新。
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;

    /**
     * 更新人。
     * <p>
     * 用于审计“是谁修改了权限规则”。
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}
