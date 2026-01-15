package com.ysmjjsy.goya.component.bus.stream.capabilities;

/**
 * <p>MQ 能力声明</p>
 * <p>用于声明远程事件发布器支持的能力，避免"能力错觉"</p>
 * <p>不同 MQ 的能力差异：</p>
 * <ul>
 *   <li>Kafka: 支持分区、顺序消息，不支持原生延迟消息（需通过 ScheduledExecutorService 实现）</li>
 *   <li>RabbitMQ: 支持延迟消息（通过插件）、顺序消息（通过单队列），不支持分区</li>
 *   <li>RocketMQ: 支持延迟消息、顺序消息、分区</li>
 * </ul>
 * <p>使用示例：</p>
 * <pre>{@code
 * Capabilities caps = publisher.getCapabilities();
 * if (caps.supportsDelayedMessages()) {
 *     busService.publishDelayed(event, Duration.ofSeconds(10));
 * } else {
 *     log.warn("Delayed messages not supported, using fallback");
 * }
 * }</pre>
 *
 * @param supportsDelayedMessages 是否支持延迟消息（原生支持，非模拟实现）
 * @param supportsOrderedMessages 是否支持顺序消息（通过分区键保证顺序）
 * @param supportsPartitioning    是否支持分区（Kafka、RocketMQ 支持）
 * @param maxDelayMillis           最大延迟时间（毫秒），-1 表示无限制
 * @param description              能力描述
 * @param allowDegradation         是否允许降级（当能力不支持时，是否允许降级为普通发布）
 * @author goya
 * @since 2025/12/21
 */
public record Capabilities(
        boolean supportsDelayedMessages,
        boolean supportsOrderedMessages,
        boolean supportsPartitioning,
        long maxDelayMillis,
        String description,
        boolean allowDegradation
) {

    /**
     * 默认能力（不支持任何高级特性，不允许降级）
     */
    public static final Capabilities NONE = new Capabilities(
            false,
            false,
            false,
            -1,
            "No advanced capabilities",
            false
    );

    /**
     * 基础能力（仅支持顺序消息，不允许降级）
     */
    public static final Capabilities BASIC = new Capabilities(
            false,
            true,
            false,
            -1,
            "Basic capabilities: ordered messages only",
            false
    );

    /**
     * 完整能力（支持所有特性，不允许降级）
     */
    public static final Capabilities FULL = new Capabilities(
            true,
            true,
            true,
            -1,
            "Full capabilities: delayed, ordered, and partitioned messages",
            false
    );

    /**
     * 默认能力（不支持任何高级特性，但允许降级）
     */
    public static final Capabilities DEFAULT = new Capabilities(
            false,
            false,
            false,
            -1,
            "No advanced capabilities (degradation allowed)",
            true
    );

    /**
     * 检查是否支持延迟消息
     *
     * @return true 如果支持
     */
    public boolean supportsDelayedMessages() {
        return supportsDelayedMessages;
    }

    /**
     * 检查是否支持顺序消息
     *
     * @return true 如果支持
     */
    public boolean supportsOrderedMessages() {
        return supportsOrderedMessages;
    }

    /**
     * 检查是否支持分区
     *
     * @return true 如果支持
     */
    public boolean supportsPartitioning() {
        return supportsPartitioning;
    }

    /**
     * 检查延迟时间是否在支持范围内
     *
     * @param delayMillis 延迟时间（毫秒）
     * @return true 如果在支持范围内
     */
    public boolean isDelaySupported(long delayMillis) {
        if (!supportsDelayedMessages) {
            return false;
        }
        if (maxDelayMillis < 0) {
            return true;
        }
        return delayMillis <= maxDelayMillis;
    }
}

