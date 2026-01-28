package com.ysmjjsy.goya.component.mybatisplus.tenant.profile;

import com.baomidou.mybatisplus.annotation.*;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.Instant;

/**
 * <p>租户画像配置实体</p>
 * <p>
 * 该实体对应表 tenant_profile，用于提供租户模式、数据源路由与 tenant line 开关等治理信息。
 *
 * <h2>版本语义</h2>
 * 使用 MyBatis-Plus {@link Version} 实现乐观锁版本字段 version：
 * <ul>
 *   <li>每次更新成功 version 自动递增</li>
 *   <li>框架缓存 key 将包含 version，从而实现配置变更快速生效</li>
 * </ul>
 * @author goya
 * @since 2026/1/28 23:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tenant_profile")
public class TenantProfileEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID（主键）。
     */
    @TableId(value = "tenant_id", type = IdType.INPUT)
    private String tenantId;

    /**
     * 租户模式：CORE_SHARED / DEDICATED_DB。
     */
    @TableField("mode")
    private TenantMode mode;

    /**
     * dynamic-datasource 的 dsKey。
     */
    @TableField("ds_key")
    private String dsKey;

    /**
     * 独库是否仍追加 tenant_id 条件。
     * <p>
     * 建议默认 true；当独库天然隔离且希望减少 SQL 条件时可置为 false。
     */
    @TableField("tenant_line_enabled")
    private Boolean tenantLineEnabled;

    /**
     * 乐观锁版本号（用于快速生效）。
     */
    @Version
    @TableField("version")
    private Long version;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}