package com.ysmjjsy.goya.component.framework.bus.message;

import java.lang.annotation.*;

/**
 * <p>标注一个方法为 Bus 消息监听器</p>
 * 用法：
 *
 * @author goya
 * @BusMessageListener(binding = "orderCreated")
 * public void on(MessageEnvelope<OrderCreated> env) { ... }
 *
 * @since 2026/1/26 23:46
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusMessageListener {

    /**
     * binding 名称，对应 framework.bus.bindings.<binding> 的 key。
     */
    String binding();

    /**
     * 可选：强制指定 binder（例如 rabbit），覆盖全局/配置默认选择。
     * 当前用于“消费端绑定选择”（如果你实现了按 listener 绑定不同 binder）。
     */
    String binder() default "";
}
