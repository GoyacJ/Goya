package com.ysmjjsy.goya.component.bus.transaction;

import com.ysmjjsy.goya.component.bus.definition.IEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>事件事务同步包装类</p>
 * <p>封装事务同步逻辑，支持事务提交和回滚的完整生命周期管理</p>
 * <p>记录事务状态，避免资源泄漏</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * EventTransactionSynchronization sync = new EventTransactionSynchronization(event, () -> {
 *     // 事务提交后执行的操作
 *     publishRemote(event);
 * });
 * TransactionSynchronizationManager.registerSynchronization(sync);
 * }</pre>
 *
 * @author goya
 * @since 2025/12/27
 */
@Slf4j
public class EventTransactionSynchronization implements TransactionSynchronization {

    private final IEvent event;
    private final Runnable afterCommitCallback;
    private final AtomicBoolean executed = new AtomicBoolean(false);
    private final AtomicBoolean committed = new AtomicBoolean(false);
    private final AtomicBoolean rolledBack = new AtomicBoolean(false);

    /**
     * 构造函数
     *
     * @param event               事件对象
     * @param afterCommitCallback 事务提交后的回调
     */
    public EventTransactionSynchronization(IEvent event, Runnable afterCommitCallback) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (afterCommitCallback == null) {
            throw new IllegalArgumentException("After commit callback cannot be null");
        }
        this.event = event;
        this.afterCommitCallback = afterCommitCallback;
    }

    @Override
    public void afterCommit() {
        if (executed.compareAndSet(false, true)) {
            committed.set(true);
            log.debug("[Goya] |- component [bus] EventTransactionSynchronization |- transaction committed for event [{}], executing callback",
                    event.eventName());
            try {
                afterCommitCallback.run();
            } catch (Exception e) {
                log.error("[Goya] |- component [bus] EventTransactionSynchronization |- failed to execute after commit callback for event [{}]: {}",
                        event.eventName(), e.getMessage(), e);
                throw e;
            }
        } else {
            log.warn("[Goya] |- component [bus] EventTransactionSynchronization |- afterCommit() already executed for event [{}]",
                    event.eventName());
        }
    }

    @Override
    public void afterCompletion(int status) {
        if (status == STATUS_ROLLED_BACK) {
            if (rolledBack.compareAndSet(false, true)) {
                log.debug("[Goya] |- component [bus] EventTransactionSynchronization |- transaction rolled back for event [{}], cleaning up",
                        event.eventName());
                // 事务回滚，清理资源
                cleanup();
            }
        } else if (status == STATUS_COMMITTED) {
            // 如果 afterCommit() 没有被调用（可能在某些边界情况下），这里作为后备
            if (!executed.get()) {
                log.warn("[Goya] |- component [bus] EventTransactionSynchronization |- transaction committed but afterCommit() was not called for event [{}]. " +
                                "This may indicate a transaction management issue.",
                        event.eventName());
                // 不执行回调，因为 afterCommit() 应该已经处理了
            }
        }
    }

    /**
     * 清理资源
     * <p>在事务回滚时调用</p>
     */
    private void cleanup() {
        // 这里可以添加清理逻辑，例如取消已注册的远程事件发布任务
        log.trace("[Goya] |- component [bus] EventTransactionSynchronization |- cleanup completed for event [{}]",
                event.eventName());
    }

    /**
     * 检查事务状态
     *
     * @return true 如果事务已提交
     */
    public boolean isCommitted() {
        return committed.get();
    }

    /**
     * 检查事务状态
     *
     * @return true 如果事务已回滚
     */
    public boolean isRolledBack() {
        return rolledBack.get();
    }

    /**
     * 检查是否已执行
     *
     * @return true 如果回调已执行
     */
    public boolean isExecuted() {
        return executed.get();
    }
}

