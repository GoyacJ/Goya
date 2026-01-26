package com.ysmjjsy.goya.component.framework.log.mdc;

import org.slf4j.MDC;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * <p>MDC 快照：用于跨线程传播 MDC</p>
 *
 * @author goya
 * @since 2026/1/24 22:04
 */
public final class MdcSnapshot implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, String> context;

    private MdcSnapshot(Map<String, String> context) {
        this.context = context == null ? null : Collections.unmodifiableMap(context);
    }

    /**
     * 捕获当前线程 MDC。
     *
     * @return snapshot
     */
    public static MdcSnapshot capture() {
        return new MdcSnapshot(MDC.getCopyOfContextMap());
    }

    /**
     * 在当前线程恢复该快照。
     */
    public void restore() {
        if (context == null || context.isEmpty()) {
            MDC.clear();
        } else {
            MDC.setContextMap(context);
        }
    }

    /**
     * 获取快照内容（只读）。
     *
     * @return map
     */
    public Map<String, String> context() {
        return context == null ? Map.of() : context;
    }
}
