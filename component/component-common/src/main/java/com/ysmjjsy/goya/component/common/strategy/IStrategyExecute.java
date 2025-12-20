package com.ysmjjsy.goya.component.common.strategy;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:50
 */
public interface IStrategyExecute<I, O> extends IStrategy {

    /**
     * 执行策略
     *
     * @param request 执行策略入参
     */
    default void execute(I request) {

    }

    /**
     * 执行策略，带返回值
     *
     * @param request 执行策略入参
     * @return 执行策略后返回值
     */
    default O executeResp(I request) {
        return null;
    }
}
