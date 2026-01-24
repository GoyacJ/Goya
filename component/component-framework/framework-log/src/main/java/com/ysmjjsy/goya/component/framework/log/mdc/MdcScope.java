package com.ysmjjsy.goya.component.framework.log.mdc;

import org.slf4j.MDC;

import java.util.Map;

/**
 * <p>MDC 作用域：用于 try-with-resources 安全设置并恢复 MDC</p>
 *
 * @author goya
 * @since 2026/1/24 22:04
 */
public final class MdcScope implements AutoCloseable {

    private final Map<String, String> previous;

    private MdcScope(Map<String, String> previous) {
        this.previous = previous;
    }

    /**
     * 捕获当前 MDC 并设置新的 MDC map。
     *
     * @param newContext 新上下文（可为空；为空则清空）
     * @return scope
     */
    public static MdcScope set(Map<String, String> newContext) {
        Map<String, String> prev = MDC.getCopyOfContextMap();
        if (newContext == null || newContext.isEmpty()) {
            MDC.clear();
        } else {
            MDC.setContextMap(newContext);
        }
        return new MdcScope(prev);
    }

    /**
     * 恢复进入作用域之前的 MDC。
     */
    @Override
    public void close() {
        if (previous == null || previous.isEmpty()) {
            MDC.clear();
        } else {
            MDC.setContextMap(previous);
        }
    }
}