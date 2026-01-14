package com.ysmjjsy.goya.component.framework.strategy.chain;

import org.springframework.core.Ordered;

/**
 * <p>抽象业务责任链组件</p>
 *
 * @author goya
 * @since 2025/12/19 23:48
 */
public interface ChainHandler<T> extends Ordered {

    /**
     * 责任链节点执行
     * @param requestParam 入参
     * @return true=继续链条，false=中断链条
     */
    boolean handle(T requestParam);

    /**
     * 责任链分组标识（每个业务定义自己的链）
     */
    String chainKey();
}
