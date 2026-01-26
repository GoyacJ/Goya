package com.ysmjjsy.goya.component.framework.bus.runtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ErrorMessage;

/**
 * <p>默认错误日志订阅者</p>
 *
 * <p>
 * Bus 提供一个全局 error 通道（{@link BusChannels#error()}），用于统一输出异常并预留扩展点。
 * 为了做到“开箱即用”，这里默认订阅该通道并记录日志。
 * <p>
 * 注意：这里不实现重试/DLQ 等策略（避免造轮子）。这些能力优先交给 Spring Kafka / Spring AMQP /
 * Spring Cloud Stream 以及 Broker 自身能力完成。
 *
 * @author goya
 * @since 2026/1/27 00:20
 */
@Slf4j
public final class BusErrorLogger implements MessageHandler {

    @Override
    public void handleMessage(Message<?> message) {
        if (message instanceof ErrorMessage em) {
            Throwable ex = em.getPayload();
            log.error("Bus 发生异常: headers={}, ex={}", em.getHeaders(), ex.toString(), ex);
            return;
        }
        log.error("Bus error 通道收到非 ErrorMessage: headers={}, payload={}", message.getHeaders(), message.getPayload());
    }
}