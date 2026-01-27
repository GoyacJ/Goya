package com.ysmjjsy.goya.component.framework.bus.message;

import org.springframework.util.StringUtils;

/**
 * <p>发送选项（一次发送的覆盖参数）</p>
 * <p>
 * 设计目标：
 * - 不造轮子：只保留“选择 binder”这种 bus 层必须的控制项
 * - 其余并发、重试、DLQ 等能力交给官方组件/配置
 *
 * @author goya
 * @since 2026/1/26 23:47
 */
public record SendOptions(
        /*
          本次发送强制使用的 binder（例如 rabbit）。
         */
        String binder
) {
    public static final SendOptions DEFAULT = new SendOptions(null);

    public static SendOptions binder(String binder) {
        if (!StringUtils.hasText(binder)) return DEFAULT;
        return new SendOptions(binder.trim());
    }

    public boolean hasBinderOverride() {
        return StringUtils.hasText(binder);
    }
}