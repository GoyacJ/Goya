package com.ysmjjsy.goya.component.bus.definition;

/**
 * <p>事件总线统一 Header 协议</p>
 * <p>定义一套与中间件无关的元数据规范，通过 Header 传递事件元信息</p>
 * <p>这些 Header 会被映射到 Spring Cloud Stream 的 Message Headers</p>
 *
 * @author goya
 * @since 2025/12/21
 * @see org.springframework.messaging.MessageHeaders
 */
public final class BusHeaders {

    private BusHeaders() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 延迟毫秒数
     * <p>用于延迟消息发布，某些 binder（如 RabbitMQ）原生支持延迟消息</p>
     * <p>Header 名称：x-goya-delay</p>
     */
    public static final String DELAY = "x-goya-delay";

    /**
     * 消息排序分区键
     * <p>用于保证同一分区键的消息有序处理</p>
     * <p>Header 名称：x-goya-partition-key</p>
     * <p>会被映射到 Spring Cloud Stream 的 partition-key-expression</p>
     */
    public static final String PARTITION_KEY = "x-goya-partition-key";

    /**
     * 链路追踪 ID
     * <p>用于分布式链路追踪，透传到 MDC</p>
     * <p>Header 名称：x-goya-trace-id</p>
     */
    public static final String TRACE_ID = "x-goya-trace-id";

    /**
     * 幂等去重键
     * <p>用于幂等性检查，防止重复处理同一事件</p>
     * <p>Header 名称：x-goya-idempotency-key</p>
     * <p>基于 ICacheService 实现分布式去重</p>
     */
    public static final String IDEMPOTENCY_KEY = "x-goya-idempotency-key";

    /**
     * 事件名称
     * <p>用于消息路由和订阅</p>
     * <p>Header 名称：x-goya-event-name</p>
     */
    public static final String EVENT_NAME = "x-goya-event-name";

    /**
     * 事件作用域
     * <p>用于控制事件的发布范围（LOCAL、REMOTE、ALL）</p>
     * <p>Header 名称：x-goya-event-scope</p>
     */
    public static final String EVENT_SCOPE = "x-goya-event-scope";

    /**
     * 事件类型（完整类名）
     * <p>用于跨服务事件反序列化，支持自动类型发现</p>
     * <p>Header 名称：x-goya-event-type</p>
     * <p>如果 Header 中的类型不存在，会通过 eventName 自动查找本地类型</p>
     */
    public static final String EVENT_TYPE = "x-goya-event-type";

    /**
     * 事件版本
     * <p>用于事件演进和版本管理</p>
     * <p>Header 名称：x-goya-event-version</p>
     */
    public static final String EVENT_VERSION = "x-goya-event-version";

    /**
     * 事件 ID
     * <p>用于事件追踪和回放，每个事件实例都有唯一的 ID</p>
     * <p>Header 名称：x-goya-event-id</p>
     */
    public static final String EVENT_ID = "x-goya-event-id";

    /**
     * 关联 ID
     * <p>用于关联相关事件，如订单创建事件和订单支付事件可以使用相同的 correlationId</p>
     * <p>Header 名称：x-goya-correlation-id</p>
     */
    public static final String CORRELATION_ID = "x-goya-correlation-id";

    /**
     * 事件来源
     * <p>用于标识事件的来源服务或系统</p>
     * <p>Header 名称：x-goya-event-source</p>
     */
    public static final String EVENT_SOURCE = "x-goya-event-source";

    /**
     * 事件标签
     * <p>用于事件分类和过滤，格式为 JSON 字符串或逗号分隔的键值对</p>
     * <p>Header 名称：x-goya-event-tags</p>
     */
    public static final String EVENT_TAGS = "x-goya-event-tags";
}

