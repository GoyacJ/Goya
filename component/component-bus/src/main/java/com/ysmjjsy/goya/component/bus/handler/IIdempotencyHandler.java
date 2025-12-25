package com.ysmjjsy.goya.component.bus.handler;

/**
 * <p>幂等性处理器接口</p>
 * <p>用于检查事件是否已处理，防止重复处理</p>
 *
 * @author goya
 * @since 2025/12/21
 */
public interface IIdempotencyHandler {

    /**
     * 检查并设置幂等键
     * <p>如果幂等键已存在，返回 false（表示已处理）</p>
     * <p>如果幂等键不存在，设置并返回 true（表示未处理）</p>
     *
     * @param idempotencyKey 幂等键
     * @return true 如果未处理（已设置），false 如果已处理
     */
    boolean checkAndSet(String idempotencyKey);
}

