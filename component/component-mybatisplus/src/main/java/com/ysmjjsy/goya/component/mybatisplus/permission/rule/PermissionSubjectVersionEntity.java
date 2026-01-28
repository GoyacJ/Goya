package com.ysmjjsy.goya.component.mybatisplus.permission.rule;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.Instant;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/29 00:20
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("permission_subject_version")
public class PermissionSubjectVersionEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -4498440061061484828L;

    /**
     * 租户 ID。
     * <p>
     * 与 subjectId 共同唯一确定一个权限主体。
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 授权主体 ID。
     * <p>
     * 与 tenantId 组合后，表示一个“权限作用主体”。
     */
    @TableField("subject_id")
    private String subjectId;

    /**
     * 权限版本号。
     * <p>
     * 初始值通常为 0 或 1。
     * 每当该 subject 的任意资源规则发生变化时，必须递增。
     *
     * <p>
     * 权限引擎会将该值作为缓存 key 的一部分，
     * 用于判断缓存是否需要失效。
     */
    @TableField("version")
    private Long version;

    /**
     * 最近一次版本变更时间。
     * <p>
     * 用于审计与排障（例如判断规则是否已成功发布）。
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
