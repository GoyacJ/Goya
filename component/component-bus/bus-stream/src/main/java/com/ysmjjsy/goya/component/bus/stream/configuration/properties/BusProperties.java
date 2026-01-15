package com.ysmjjsy.goya.component.bus.stream.configuration.properties;

import com.ysmjjsy.goya.component.bus.stream.constants.BusStreamConst;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;
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
@ConfigurationProperties(prefix = BusStreamConst.PROPERTY_BUS_STREAM)
public record BusProperties(
        /*
          默认远程总线标记
         */
        @Schema(description = "默认远程总线标记", example = "REMOTE")
        @DefaultValue(BusStreamConst.MARK_REMOTE)
        String defaultRemoteBus,

        /*
          幂等性配置
         */
        @Schema(description = "幂等性配置")
        @DefaultValue
        Idempotency idempotency,

        /*
          Destination 配置
         */
        @Schema(description = "Destination 配置")
        @DefaultValue
        Destination destination,

        /*
          反序列化配置
         */
        @Schema(description = "反序列化配置")
        @DefaultValue
        Deserialization deserialization,

        /*
          能力配置
         */
        @Schema(description = "能力配置")
        @DefaultValue
        CapabilitiesConfig capabilities,

        /*
          监听器配置
         */
        @Schema(description = "监听器配置")
        @DefaultValue
        ListenerConfig listener
) {
    /**
     * 幂等性配置
     */
    @Schema(description = "幂等性配置")
    public record Idempotency(
            /*
              是否启用幂等性检查
             */
            @Schema(description = "是否启用幂等性检查", example = "true")
            @DefaultValue("true")
            Boolean enabled,

            /*
              缓存名称
             */
            @Schema(description = "缓存名称", example = "bus:idempotency")
            @DefaultValue("bus:idempotency")
            String cacheName,

            /*
              TTL（生存时间）
             */
            @Schema(description = "TTL（生存时间）", example = "PT24H")
            @DefaultValue("PT24H")
            Duration ttl
    ) {
    }

    /**
     * Destination 配置
     */
    @Schema(description = "Destination 配置")
    public record Destination(
            /*
              默认模板
              <p>支持占位符：{eventName} 会被替换为事件名称</p>
              <p>示例：bus.{eventName} -> bus.order-created</p>
             */
            @Schema(description = "默认模板", example = "bus.{eventName}")
            @DefaultValue("bus.{eventName}")
            String defaultTemplate,

            /*
              事件名称到 destination 的映射
              <p>key: 事件名称（如 "order.created"）</p>
              <p>value: destination（如 "bus.order-created"）</p>
             */
            @Schema(description = "事件名称到 destination 的映射")
            @DefaultValue
            Map<String, String> mappings
    ) {
    }

    /**
     * 反序列化配置
     */
    @Schema(description = "反序列化配置")
    public record Deserialization(
            /*
              允许加载的事件类包名前缀列表
              <p>默认只允许加载 com.ysmjjsy.goya 包下的类</p>
              <p>如果事件类在其他包中，需要添加对应的包名前缀</p>
             */
            @Schema(description = "允许加载的事件类包名前缀列表", example = "[\"com.ysmjjsy.goya\", \"com.example.events\"]")
            @DefaultValue("[\"com.ysmjjsy.goya\"]")
            List<String> allowedPackages
    ) {
    }

    /**
     * 能力配置
     */
    @Schema(description = "能力配置")
    public record CapabilitiesConfig(
            /*
              是否允许降级
              <p>当 MQ 不支持某个能力（如延迟消息、顺序消息）时，是否允许降级为普通发布</p>
              <p>默认值为 false，不允许降级</p>
              <p>如果设置为 true，当能力不支持时会降级为普通发布并记录警告日志</p>
              <p>如果设置为 false，当能力不支持时会抛出异常</p>
             */
            @Schema(description = "是否允许降级", example = "false")
            @DefaultValue("false")
            Boolean allowDegradation
    ) {
    }

    /**
     * 监听器配置
     */
    @Schema(description = "监听器配置")
    public record ListenerConfig(
            /*
              业务异常时是否继续执行其他监听器
              <p>默认值为 false，业务异常时继续执行其他监听器</p>
              <p>如果设置为 true，业务异常时会中断后续监听器的执行</p>
              <p>系统异常（如 RuntimeException、SystemException）总是会中断执行</p>
             */
            @Schema(description = "业务异常时是否继续执行其他监听器", example = "false")
            @DefaultValue("false")
            Boolean continueOnBusinessException
    ) {
    }
}
