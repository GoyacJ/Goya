package com.ysmjjsy.goya.component.bus.configuration.properties;

import com.ysmjjsy.goya.component.bus.constants.IBusConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>事件总线配置属性</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * goya:
 *   bus:
 *     defaultRemoteBus: "REMOTE"
 *     idempotency:
 *       enabled: true
 *       cacheName: "bus:idempotency"
 *       ttl: PT24H
 *     destination:
 *       defaultTemplate: "bus.{eventName}"
 *       mappings:
 *         "order.created": "bus.order-created"
 *         "user.updated": "bus.user-updated"
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
@Schema(description = "事件总线配置属性")
@ConfigurationProperties(prefix = IBusConstants.PROPERTY_BUS)
public record BusProperties(
        /**
         * 默认远程总线标记
         */
        @Schema(description = "默认远程总线标记", example = "REMOTE")
        @DefaultValue("REMOTE")
        String defaultRemoteBus,

        /**
         * 幂等性配置
         */
        @Schema(description = "幂等性配置")
        @DefaultValue
        Idempotency idempotency,

        /**
         * Destination 配置
         */
        @Schema(description = "Destination 配置")
        @DefaultValue
        Destination destination
) {
    /**
     * 幂等性配置
     */
    @Schema(description = "幂等性配置")
    public record Idempotency(
            /**
             * 是否启用幂等性检查
             */
            @Schema(description = "是否启用幂等性检查", example = "true")
            @DefaultValue("true")
            Boolean enabled,

            /**
             * 缓存名称
             */
            @Schema(description = "缓存名称", example = "bus:idempotency")
            @DefaultValue("bus:idempotency")
            String cacheName,

            /**
             * TTL（生存时间）
             */
            @Schema(description = "TTL（生存时间）", example = "PT24H")
            @DefaultValue("PT24H")
            Duration ttl
    ) {
        /**
         * 获取是否启用幂等性检查，提供默认值
         *
         * @return 是否启用
         */
        public Boolean enabled() {
            return enabled != null ? enabled : true;
        }

        /**
         * 获取缓存名称，提供默认值
         *
         * @return 缓存名称
         */
        public String cacheName() {
            return cacheName != null ? cacheName : "bus:idempotency";
        }

        /**
         * 获取 TTL，提供默认值
         *
         * @return TTL
         */
        public Duration ttl() {
            return ttl != null ? ttl : Duration.ofHours(24);
        }
    }

    /**
     * Destination 配置
     */
    @Schema(description = "Destination 配置")
    public record Destination(
            /**
             * 默认模板
             * <p>支持占位符：{eventName} 会被替换为事件名称</p>
             * <p>示例：bus.{eventName} -> bus.order-created</p>
             */
            @Schema(description = "默认模板", example = "bus.{eventName}")
            @DefaultValue("bus.{eventName}")
            String defaultTemplate,

            /**
             * 事件名称到 destination 的映射
             * <p>key: 事件名称（如 "order.created"）</p>
             * <p>value: destination（如 "bus.order-created"）</p>
             */
            @Schema(description = "事件名称到 destination 的映射")
            @DefaultValue
            Map<String, String> mappings
    ) {
        /**
         * 获取默认模板，提供默认值
         *
         * @return 默认模板
         */
        public String defaultTemplate() {
            return defaultTemplate != null ? defaultTemplate : "bus.{eventName}";
        }

        /**
         * 获取映射表，提供默认值
         *
         * @return 映射表
         */
        public Map<String, String> mappings() {
            return mappings != null ? mappings : new HashMap<>();
        }
    }

    /**
     * 获取默认远程总线标记，提供默认值
     *
     * @return 默认远程总线标记
     */
    public String defaultRemoteBus() {
        return defaultRemoteBus != null ? defaultRemoteBus : IBusConstants.MARK_REMOTE;
    }

    /**
     * 获取幂等性配置，提供默认值
     *
     * @return 幂等性配置
     */
    public Idempotency idempotency() {
        return idempotency != null ? idempotency : new Idempotency(true, "bus:idempotency", Duration.ofHours(24));
    }

    /**
     * 获取 Destination 配置，提供默认值
     *
     * @return Destination 配置
     */
    public Destination destination() {
        return destination != null ? destination : new Destination("bus.{eventName}", new HashMap<>());
    }
}
