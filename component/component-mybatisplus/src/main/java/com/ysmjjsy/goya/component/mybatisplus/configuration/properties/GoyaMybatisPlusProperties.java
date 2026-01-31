package com.ysmjjsy.goya.component.mybatisplus.configuration.properties;

import com.ysmjjsy.goya.component.mybatisplus.constants.MybatisPlusConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * <p>配置文件</p>
 * 注意：本模块只使用配置文件控制“开关与默认策略”，
 * 权限规则本身不通过配置文件表达（规则来自存储与运行时编译）。
 *
 * <p><b>关键约束：</b>
 * <ul>
 *   <li>多租户 requireTenant=true 时，tenant 缺失必须拒绝</li>
 *   <li>权限 failClosed=true 时，AccessContext/规则异常默认返回 1=0</li>
 *   <li>插件链顺序固定，不允许通过配置改变执行顺序</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 09:46
 */
@ConfigurationProperties(prefix = MybatisPlusConst.PROPERTY_MYBATIS_PLUS)
public record GoyaMybatisPlusProperties(
        /* 多租户相关配置。 */
        @DefaultValue
        Tenant tenant,

        /* 动态权限相关配置。 */
        @DefaultValue
        Permission permission
) {

    /**
     * 多租户配置。
     */
    public record Tenant(
            /*
              是否启用多租户能力（路由 + TenantLine）。
             */
            @DefaultValue("true")
            boolean enabled,

            /*
              是否要求 tenant 必须存在。
             */
            @DefaultValue("true")
            boolean requireTenant,

            /*
              租户列默认字段名。
             */
            @DefaultValue("tenant_id")
            String tenantIdColumn,

            /*
              静态忽略表（不追加 tenant 条件）。
             */
            @DefaultValue
            String[] ignoreTables
    ) {
    }

    /**
     * 动态权限配置（只控制开关与安全策略，不表达规则本身）。
     */
    public record Permission(
            /*
              是否启用动态数据权限。
             */
            @DefaultValue("true")
            boolean enabled,

            /*
              异常时是否 fail-closed（1=0）。
             */
            @DefaultValue("true")
            boolean failClosed,

            /*
              是否将权限应用到写操作（默认 false，不建议开启）。
             */
            @DefaultValue("false")
            boolean applyToWrite
    ) {
    }
}
