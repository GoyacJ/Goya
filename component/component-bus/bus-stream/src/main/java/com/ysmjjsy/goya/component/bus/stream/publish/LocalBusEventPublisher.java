package com.ysmjjsy.goya.component.bus.stream.publish;

import com.ysmjjsy.goya.component.bus.stream.constants.BusStreamConst;
import com.ysmjjsy.goya.component.bus.stream.definition.IBusEvent;
import com.ysmjjsy.goya.component.bus.stream.processor.BusEventListenerHandler;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.stragegy.StrategyExecute;
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
public class LocalBusEventPublisher implements StrategyExecute<IBusEvent, Void> {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectProvider<BusEventListenerHandler> handlerProvider;

    @Override
    public String mark() {
        return BusStreamConst.MARK_LOCAL;
    }

    @Override
    public void execute(IBusEvent request) {
        if (request == null) {
            throw new CommonException("Event cannot be null");
        }

        log.debug("[Goya] |- component [bus] LocalEventPublisher |- publish local event [{}]", request.eventName());
        
        // 通过 BusEventListenerHandler 进行过滤和路由
        BusEventListenerHandler handler = handlerProvider.getIfAvailable();
        if (handler == null) {
            throw new CommonException("BusEventListenerHandler is not available. Please ensure component-bus is properly configured.");
        }
        
        // 创建本地事件的 Message，用于统一处理
        Message<IBusEvent> message = MessageBuilder.withPayload(request).build();
        handler.handleLocalEvent(message);
    }

    @Override
    public Void executeResp(IBusEvent request) {
        execute(request);
        return null;
    }
}

