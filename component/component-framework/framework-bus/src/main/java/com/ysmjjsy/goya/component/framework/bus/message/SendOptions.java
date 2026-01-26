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
          本次发送强制指定的 binder 名（例如 "rabbit"）。
          为空表示不覆盖，走 binding 配置或全局默认 binder。
         */
        String binder
) {

    /**
     * 默认发送选项：不覆盖 binder。
     */
    public static final SendOptions DEFAULT = new SendOptions(null);

    /**
     * 快捷构造：强制指定 binder。
     */
    public static SendOptions binder(String binder) {
        if (!StringUtils.hasText(binder)) {
            return DEFAULT;
        }
        return new SendOptions(binder.trim());
    }

    /**
     * 是否指定了 binder 覆盖。
     */
    public boolean hasBinderOverride() {
        return StringUtils.hasText(binder);
    }
}