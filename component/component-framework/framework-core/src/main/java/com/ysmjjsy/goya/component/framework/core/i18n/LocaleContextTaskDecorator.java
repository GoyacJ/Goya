package com.ysmjjsy.goya.component.framework.core.i18n;

import org.jspecify.annotations.NullMarked;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.task.TaskDecorator;

/**
 * <p>Locale 上下文任务装饰器，用于在线程池异步执行时传播 LocaleContext</p>
 * 适用于 @Async、线程池、异步任务等场景，确保下游 i18n 解析保持一致语言
 *
 * @author goya
 * @since 2026/1/24 15:31
 */
public class LocaleContextTaskDecorator implements TaskDecorator {

    /**
     * 装饰任务，传播当前线程的 LocaleContext 到异步线程。
     *
     * @param runnable 原始任务
     * @return 装饰后的任务
     */
    @Override
    @NullMarked
    public Runnable decorate(Runnable runnable) {
        LocaleContext context = LocaleContextHolder.getLocaleContext();
        return () -> {
            LocaleContext previous = LocaleContextHolder.getLocaleContext();
            try {
                LocaleContextHolder.setLocaleContext(context);
                runnable.run();
            } finally {
                LocaleContextHolder.setLocaleContext(previous);
            }
        };
    }
}
