package com.ysmjjsy.goya.component.bus.processor;

import com.ysmjjsy.goya.component.bus.definition.EventScope;
import com.ysmjjsy.goya.component.bus.definition.IEvent;
import com.ysmjjsy.goya.component.bus.deserializer.DeserializationResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>事件处理上下文</p>
 * <p>在 Pipeline 中传递事件处理相关的所有信息</p>
 * <p><strong>设计说明：</strong></p>
 * <ul>
 *   <li>Builder 模式用于初始化：使用 Lombok 的 @Builder 注解，方便创建初始状态的 EventContext。</li>
 *   <li>字段可变用于拦截器链状态传递：虽然使用 Builder 初始化，但字段使用 @Setter 标记为可变，
 *       这是为了在拦截器链中传递和修改状态（如 deserializationResult、matchedListeners、aborted 等）。</li>
 *   <li>线程安全：每个事件处理流程都有独立的 EventContext 实例，不存在线程安全问题。</li>
 * </ul>
 * <p>使用示例：</p>
 * <pre>{@code
 * EventContext context = EventContext.builder()
 *     .message(message)
 *     .scope(EventScope.REMOTE)
 *     .build();
 *
 * // 在 Interceptor 中使用
 * if (context.isAborted()) {
 *     return; // 已中止，不再处理
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
@Getter
@Setter
@Builder
public class EventContext {

    /**
     * 原始消息
     */
    private Message<?> message;

    /**
     * 事件作用域
     */
    private EventScope scope;

    /**
     * 反序列化结果
     */
    private DeserializationResult deserializationResult;

    /**
     * 事件对象（反序列化后）
     */
    private IEvent event;

    /**
     * 匹配的监听器列表
     */
    @Builder.Default
    private List<BusEventListenerScanner.EventListenerMetadata> matchedListeners = new ArrayList<>();

    /**
     * 是否已中止处理
     * <p>如果为 true，后续 Interceptor 应该跳过处理</p>
     */
    @Builder.Default
    private boolean aborted = false;

    /**
     * 中止原因
     */
    private String abortReason;

    /**
     * 中止处理
     *
     * @param reason 中止原因
     */
    public void abort(String reason) {
        this.aborted = true;
        this.abortReason = reason;
    }

    /**
     * 检查是否已中止
     *
     * @return true 如果已中止
     */
    public boolean isAborted() {
        return aborted;
    }
}

