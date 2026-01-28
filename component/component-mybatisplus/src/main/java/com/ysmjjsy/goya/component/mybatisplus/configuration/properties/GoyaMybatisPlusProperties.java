package com.ysmjjsy.goya.component.mybatisplus.configuration.properties;

import com.ysmjjsy.goya.component.mybatisplus.constants.MybatisPlusConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

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
        Permission permission,

        /* 安全护栏配置。 */
        @DefaultValue
        Safety safety
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
              是否强制要求 tenant 存在。
              <p>
              生产建议 true：tenant 缺失直接拒绝，避免“默认落核心库”导致串数据。
             */
            @DefaultValue("true")
            boolean requireTenant
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
              失败是否闭合（Fail Closed）。
              <p>
              true：AccessContext 缺失/规则编译失败/类型不匹配 => where 1=0
              false：忽略该规则并告警（存在放行风险）
             */
            @DefaultValue("true")
            boolean failClosed,

            /*
              L2 编译结果缓存 TTL。
              <p>
              key 已包含 version，因此 TTL 主要用于淘汰旧版本占用。
              null/<=0 表示使用 cacheName 默认 TTL。
             */
            @DefaultValue("PT2M")
            Duration compiledCacheTtl
    ) {
    }

    /**
     * 安全护栏配置。
     */
    public record Safety(
            /*
              是否启用 BlockAttack（阻断无 WHERE 的 update/delete）。
             */
            @DefaultValue("true")
            boolean blockAttack
    ) {
    }
}
