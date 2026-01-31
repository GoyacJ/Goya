package com.ysmjjsy.goya.component.mybatisplus.context;

import lombok.experimental.UtilityClass;

/**
 * <p>访问上下文（线程级）</p>
 *
 * <p>用于承载 SubjectContext 所需信息。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@UtilityClass
public class AccessContext {

    private static final ThreadLocal<AccessContextValue> CONTEXT = new ThreadLocal<>();

    /**
     * 设置访问上下文。
     *
     * @param value 上下文值
     */
    public void set(AccessContextValue value) {
        CONTEXT.set(value);
    }

    /**
     * 获取访问上下文。
     *
     * @return 上下文值
     */
    public AccessContextValue get() {
        AccessContextValue value = CONTEXT.get();
        return value == null ? AccessContextValue.empty() : value;
    }

    /**
     * 清理访问上下文。
     */
    public void clear() {
        CONTEXT.remove();
    }
}
