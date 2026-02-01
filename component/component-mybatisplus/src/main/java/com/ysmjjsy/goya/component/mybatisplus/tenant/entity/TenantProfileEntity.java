package com.ysmjjsy.goya.component.mybatisplus.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.constants.MybatisPlusConst;
import com.ysmjjsy.goya.component.mybatisplus.definition.AuditEntity;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantDataSourceType;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>租户配置表实体</p>
 *
 * @author goya
 * @since 2026/1/31 12:10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tenant_profile")
public class TenantProfileEntity extends AuditEntity {

    @Serial
    private static final long serialVersionUID = 5113821273566152101L;

    /**
     * 租户 ID（主键）。
     */
    @TableId(value = MybatisPlusConst.FIELD_TENANT_ID, type = IdType.INPUT)
    private String tenantId;

    /**
     * 租户模式（CORE_SHARED/DEDICATED_DB）。
     */
    @TableField("mode")
    private TenantMode mode;

    /**
     * 数据源 key。
     */
    @TableField("ds_key")
    private String dsKey;

    /**
     * JDBC URL。
     */
    @TableField("jdbc_url")
    private String jdbcUrl;

    /**
     * 用户名。
     */
    @TableField("jdbc_username")
    private String jdbcUsername;

    /**
     * 密码。
     */
    @TableField("jdbc_password")
    private String jdbcPassword;

    /**
     * 驱动类名。
     */
    @TableField("jdbc_driver")
    private String jdbcDriver;

    /**
     * 数据源类型（MYSQL/POSTGRESQL/SQLITE）。
     */
    @TableField("ds_type")
    private TenantDataSourceType dsType;

    /**
     * 是否启用 tenant 过滤。
     */
    @TableField("tenant_line_enabled")
    private Boolean tenantLineEnabled;

    /**
     * 配置版本号。
     */
    @TableField("tenant_version")
    private Long tenantVersion;
}
