package com.ysmjjsy.goya.component.cache.multilevel.ttl;

import com.ysmjjsy.goya.component.cache.multilevel.core.GoyaCache;
import com.ysmjjsy.goya.component.cache.multilevel.core.LocalCache;
import org.springframework.cache.Cache;

/**
 * 降级策略接口
 *
 * <p>定义当远程缓存（L2）操作失败时的降级处理策略，确保系统的可用性和可靠性。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>处理 L2 缓存故障时的降级逻辑</li>
 *   <li>提供多种降级策略以满足不同业务场景</li>
 *   <li>确保系统在部分故障时仍能提供服务</li>
 * </ul>
 *
 * <p><b>调用关系：</b>
 * <ul>
 *   <li>由 {@link GoyaCache} 在 L2 操作失败时调用</li>
 *   <li>根据配置的 {@link Type} 选择具体的降级策略实现</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:26
 */

public interface FallbackStrategy {

    /**
     * L2 查询失败时的降级处理
     *
     * <p>当 L2 缓存查询失败（网络异常、超时等）时，根据降级策略决定如何处理。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>根据策略类型决定降级行为</li>
     *   <li>如果降级到 L1，查询 L1 缓存（即使可能已过期）</li>
     *   <li>如果快速失败，抛出异常</li>
     *   <li>如果忽略错误，返回 null（触发方法执行）</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果策略为 {@link Type#FAIL_FAST}，抛出 {@link RuntimeException}（包装原始异常）</li>
     *   <li>如果策略为 {@link Type#DEGRADE_TO_L1} 且 L1 查询也失败，返回 null（触发方法执行）</li>
     *   <li>如果策略为 {@link Type#IGNORE}，返回 null（触发方法执行）</li>
     * </ul>
     *
     * @param key 缓存键
     * @param l1 本地缓存实例，用于降级查询
     * @param exception L2 查询失败的异常
     * @return 降级后的 ValueWrapper，如果降级失败或策略为 IGNORE，返回 null
     * @throws RuntimeException 如果策略为 FAIL_FAST
     */
    Cache.ValueWrapper onL2Failure(Object key, LocalCache l1, Exception exception);

    /**
     * L2 写入失败时的降级处理
     *
     * <p>当 L2 缓存写入失败时，根据降级策略决定如何处理。通常继续写入 L1，确保当前节点可用。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>根据策略类型决定降级行为</li>
     *   <li>如果降级到 L1，继续写入 L1（保证当前节点可用）</li>
     *   <li>如果快速失败，抛出异常（中断操作）</li>
     *   <li>如果忽略错误，记录日志但不中断操作</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果策略为 {@link Type#FAIL_FAST}，抛出 {@link RuntimeException}（包装原始异常）</li>
     *   <li>如果策略为 {@link Type#DEGRADE_TO_L1} 且 L1 写入也失败，记录错误日志但不抛出异常</li>
     *   <li>如果策略为 {@link Type#IGNORE}，记录警告日志但不抛出异常</li>
     * </ul>
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param l1 本地缓存实例，用于降级写入
     * @param exception L2 写入失败的异常
     * @throws RuntimeException 如果策略为 FAIL_FAST
     */
    void onL2WriteFailure(Object key, Object value, LocalCache l1, Exception exception);

    /**
     * 降级策略类型
     */
    enum Type {
        /**
         * 降级到 L1
         *
         * <p>当 L2 失败时，尝试从 L1 获取数据（即使可能已过期）。
         * 如果 L1 也没有数据，返回 null（触发方法执行）。
         *
         * <p><b>适用场景：</b>
         * <ul>
         *   <li>对可用性要求高的场景</li>
         *   <li>可以接受短暂的数据不一致</li>
         * </ul>
         */
        DEGRADE_TO_L1,

        /**
         * 快速失败
         *
         * <p>当 L2 失败时，立即抛出异常，不尝试降级。
         *
         * <p><b>适用场景：</b>
         * <ul>
         *   <li>对数据一致性要求极高的场景</li>
         *   <li>可以接受服务暂时不可用</li>
         * </ul>
         */
        FAIL_FAST,

        /**
         * 忽略错误
         *
         * <p>当 L2 失败时，记录日志但继续执行，返回 null（触发方法执行）。
         *
         * <p><b>适用场景：</b>
         * <ul>
         *   <li>对性能要求高的场景</li>
         *   <li>可以接受 L2 故障时直接查询数据源</li>
         * </ul>
         */
        IGNORE
    }
}

