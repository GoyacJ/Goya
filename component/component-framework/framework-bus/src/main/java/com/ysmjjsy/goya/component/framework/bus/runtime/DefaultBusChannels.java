package com.ysmjjsy.goya.component.framework.bus.runtime;

import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.SubscribableChannel;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>Bus 通道默认实现</p>
 * - outbound：ExecutorChannel（避免发送端被消费端逻辑阻塞）
 * - inbound：ExecutorChannel（避免 MQ listener 线程被业务处理长期占用）
 * - error：PublishSubscribeChannel（允许多个订阅者：日志/告警/指标/审计...）
 * <p>
 * 线程池由 Spring 管理：优先复用应用的 TaskExecutor（官方推荐）。
 *
 * @author goya
 * @since 2026/1/27 00:32
 */
public final class DefaultBusChannels implements BusChannels {

    private final TaskExecutor executor;
    private final ConcurrentMap<String, SubscribableChannel> outbound = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SubscribableChannel> inbound = new ConcurrentHashMap<>();
    private final SubscribableChannel error;

    public DefaultBusChannels(TaskExecutor executor) {
        this.executor = Objects.requireNonNull(executor);
        this.error = new PublishSubscribeChannel(executor);
    }

    @Override
    public SubscribableChannel outbound(String bindingName) {
        Objects.requireNonNull(bindingName, "bindingName 不能为空");
        return outbound.computeIfAbsent(bindingName, k -> new ExecutorChannel(executor));
    }

    @Override
    public SubscribableChannel inbound(String bindingName) {
        Objects.requireNonNull(bindingName, "bindingName 不能为空");
        return inbound.computeIfAbsent(bindingName, k -> new ExecutorChannel(executor));
    }

    @Override
    public SubscribableChannel error() {
        return error;
    }
}