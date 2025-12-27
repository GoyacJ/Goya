package com.ysmjjsy.goya.component.bus.processor.interceptor;

import com.ysmjjsy.goya.component.bus.handler.IIdempotencyHandler;
import com.ysmjjsy.goya.component.bus.processor.EventContext;
import com.ysmjjsy.goya.component.bus.processor.IEventInterceptor;
import com.ysmjjsy.goya.component.bus.publish.MetadataAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

/**
 * <p>幂等性检查拦截器</p>
 * <p>负责全局幂等性检查，如果事件已处理则中止处理</p>
 * <p>监听器级别的幂等性检查在 InvokeInterceptor 中执行</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class IdempotencyInterceptor implements IEventInterceptor {

    private final ObjectProvider<IIdempotencyHandler> idempotencyHandlerProvider;

    @Override
    public void intercept(EventContext context) {
        if (context.isAborted()) {
            return;
        }

        // 仅对远程事件进行全局幂等性检查
        if (context.getScope() == null || !context.getScope().name().equals("REMOTE")) {
            return;
        }

        IIdempotencyHandler idempotencyHandler = idempotencyHandlerProvider.getIfAvailable();
        if (idempotencyHandler == null) {
            log.debug("[Goya] |- component [bus] IdempotencyInterceptor |- IIdempotencyHandler not available, skip");
            return;
        }

        // 从 Message Headers 中提取幂等键
        String globalIdempotencyKey = MetadataAccessor.getIdempotencyKey(context.getMessage().getHeaders());
        if (globalIdempotencyKey == null || globalIdempotencyKey.isBlank()) {
            log.debug("[Goya] |- component [bus] IdempotencyInterceptor |- no idempotency key, skip");
            return;
        }

        // 使用原子操作检查幂等性
        if (!idempotencyHandler.checkAndSetAtomic(globalIdempotencyKey)) {
            log.debug("[Goya] |- component [bus] IdempotencyInterceptor |- event already processed globally, abort: [{}]",
                    globalIdempotencyKey);
            context.abort("Event already processed globally: " + globalIdempotencyKey);
            return;
        }

        log.trace("[Goya] |- component [bus] IdempotencyInterceptor |- idempotency check passed: [{}]",
                globalIdempotencyKey);
    }

    @Override
    public int getOrder() {
        return 200;
    }
}

