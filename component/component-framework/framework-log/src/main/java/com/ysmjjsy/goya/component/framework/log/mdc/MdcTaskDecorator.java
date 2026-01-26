package com.ysmjjsy.goya.component.framework.log.mdc;

import org.springframework.core.task.TaskDecorator;

/**
 * <p>MDC 任务装饰器：用于线程池异步执行时传播 MDC</p>
 *
 * @author goya
 * @since 2026/1/24 22:05
 */
public class MdcTaskDecorator implements TaskDecorator {

    /**
     * 装饰任务以传播 MDC。
     *
     * @param runnable 原任务
     * @return 新任务
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        MdcSnapshot snapshot = MdcSnapshot.capture();
        return () -> {
            try (MdcScope ignored = MdcScope.set(snapshot.context())) {
                runnable.run();
            }
        };
    }
}
