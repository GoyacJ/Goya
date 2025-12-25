package com.ysmjjsy.goya.component.bus.configuration.properties;

import com.ysmjjsy.goya.component.bus.constants.IBusConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>事件总线配置属性</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * goya:
 *   bus:
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
@Data
@ConfigurationProperties(prefix = IBusConstants.PROPERTY_BUS)
public class BusProperties {

    /**
     * 幂等性配置
     */
    private Idempotency idempotency = new Idempotency();

    /**
     * Destination 配置
     */
    private Destination destination = new Destination();

    /**
     * 幂等性配置
     */
    @Data
    public static class Idempotency {
        /**
         * 是否启用幂等性检查
         */
        private boolean enabled = true;

        /**
         * 缓存名称
         */
        private String cacheName = "bus:idempotency";

        /**
         * TTL（生存时间）
         */
        private Duration ttl = Duration.ofHours(24);
    }

    /**
     * Destination 配置
     */
    @Data
    public static class Destination {
        /**
         * 默认模板
         * <p>支持占位符：{eventName} 会被替换为事件名称</p>
         * <p>示例：bus.{eventName} -> bus.order-created</p>
         */
        private String defaultTemplate = "bus.{eventName}";

        /**
         * 事件名称到 destination 的映射
         * <p>key: 事件名称（如 "order.created"）</p>
         * <p>value: destination（如 "bus.order-created"）</p>
         */
        private Map<String, String> mappings = new HashMap<>();
    }
}

