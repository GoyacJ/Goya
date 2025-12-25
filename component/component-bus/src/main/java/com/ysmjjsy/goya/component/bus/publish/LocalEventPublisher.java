package com.ysmjjsy.goya.component.bus.publish;

import com.ysmjjsy.goya.component.bus.constants.IBusConstants;
import com.ysmjjsy.goya.component.bus.definition.IEvent;
import com.ysmjjsy.goya.component.bus.processor.BusEventListenerHandler;
import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import com.ysmjjsy.goya.component.common.strategy.IStrategyExecute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * <p>本地事件发布器</p>
 * <p>基于 Spring ApplicationEventPublisher 实现本地事件发布</p>
 * <p>发布前会通过 BusEventListenerHandler 进行过滤和路由</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 通过 StrategyChoose 选择发布策略
 * StrategyChoose strategyChoose;
 * IStrategyExecute publisher = strategyChoose.choose("LOCAL");
 * publisher.execute(event);
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 * @see ApplicationEventPublisher
 * @see <a href="https://docs.spring.io/spring-framework/reference/core/events.html">Spring Events</a>
 */
@Slf4j
@RequiredArgsConstructor
public class LocalEventPublisher implements IStrategyExecute<IEvent, Void> {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectProvider<BusEventListenerHandler> handlerProvider;

    @Override
    public String mark() {
        return IBusConstants.MARK_LOCAL;
    }

    @Override
    public void execute(IEvent request) {
        if (request == null) {
            throw new CommonException("Event cannot be null");
        }

        log.debug("[Goya] |- component [bus] LocalEventPublisher |- publish local event [{}]", request.eventName());
        
        // 先通过 BusEventListenerHandler 进行过滤和路由（如果存在）
        BusEventListenerHandler handler = handlerProvider.getIfAvailable();
        if (handler != null) {
            // 创建本地事件的 Message，用于统一处理
            Message<IEvent> message = MessageBuilder.withPayload(request).build();
            handler.handleLocalEvent(message);
        } else {
            // 如果没有 handler，直接发布（向后兼容）
            eventPublisher.publishEvent(request);
        }
    }

    @Override
    public Void executeResp(IEvent request) {
        execute(request);
        return null;
    }
}

