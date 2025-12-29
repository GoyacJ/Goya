package com.ysmjjsy.goya.component.cache.template;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Objects;

/**
 * 抽象限流缓存模板
 *
 * <p>基于 {@link AbstractCacheTemplate} 扩展，提供限流功能。
 * 支持滑动窗口、令牌桶、固定窗口等多种限流算法，适用于 API 限流、用户行为限流等场景。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供多种限流算法，满足不同业务场景</li>
 *   <li>支持分布式限流，保证多节点一致性</li>
 *   <li>支持动态配置，灵活调整限流策略</li>
 *   <li>自动降级策略，确保功能始终可用</li>
 * </ul>
 *
 * <p><b>核心概念：</b>
 * <ul>
 *   <li><b>滑动窗口</b>：在时间窗口内统计请求数，窗口随时间滑动</li>
 *   <li><b>令牌桶</b>：以固定速率生成令牌，请求消耗令牌</li>
 *   <li><b>固定窗口</b>：在固定时间窗口内统计请求数，窗口不滑动</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>API 限流（QPS 限制）</li>
 *   <li>用户行为限流（防刷）</li>
 *   <li>短信发送限流</li>
 *   <li>邮件发送限流</li>
 *   <li>登录失败次数限制</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * @Component
 * public class ApiRateLimiter extends AbstractRateLimitTemplate<String> {
 *
 *     @Override
 *     protected String getCacheName() {
 *         return "apiRateLimit";
 *     }
 *
 *     // 检查 API 限流
 *     public boolean checkRateLimit(String apiKey) {
 *         // 每秒最多 100 个请求
 *         return tryAcquire(apiKey, 100, Duration.ofSeconds(1));
 *     }
 * }
 * }</pre>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法都是线程安全的</li>
 *   <li>限流操作使用 GoyaCache 的缓存操作，自动处理降级策略</li>
 *   <li>缓存操作委托给线程安全的 GoyaCache</li>
 * </ul>
 *
 * @param <K> 限流键类型
 * @author goya
 * @since 2025/12/29
 */
@Slf4j
public abstract class AbstractRateLimitTemplate<K> extends AbstractCacheTemplate<K, AbstractRateLimitTemplate.RateLimitInfo> {

    /**
     * 滑动窗口限流
     *
     * <p>在时间窗口内统计请求数，窗口随时间滑动。
     * 如果请求数未超过限制，返回 true；否则返回 false。
     *
     * <p><b>算法说明：</b>
     * <ul>
     *   <li>使用缓存存储时间戳和计数</li>
     *   <li>每次请求时，清理过期的时间戳</li>
     *   <li>统计窗口内的请求数，判断是否超过限制</li>
     * </ul>
     *
     * @param key 限流键
     * @param maxRequests 最大请求数
     * @param window 时间窗口
     * @return true 如果允许请求，false 如果超过限制
     */
    public boolean tryAcquire(K key, int maxRequests, Duration window) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("Max requests must be positive");
        }
        if (window == null || window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("Window must be positive");
        }

        RateLimitInfo info = get(key);
        long now = System.currentTimeMillis();
        long windowStart = now - window.toMillis();

        if (info == null) {
            // 首次请求，创建限流信息
            RateLimitInfo newInfo = new RateLimitInfo(1, now, window.toMillis());
            put(key, newInfo, window);
            return true;
        }

        // 清理过期的时间戳
        long lastRequestTime = info.lastRequestTime();
        if (lastRequestTime < windowStart) {
            // 窗口已过期，重置计数
            RateLimitInfo newInfo = new RateLimitInfo(1, now, window.toMillis());
            put(key, newInfo, window);
            return true;
        }

        // 检查是否超过限制
        int currentCount = info.count();
        if (currentCount >= maxRequests) {
            return false;
        }

        // 更新计数
        RateLimitInfo updatedInfo = new RateLimitInfo(currentCount + 1, now, window.toMillis());
        put(key, updatedInfo, window);
        return true;
    }

    /**
     * 令牌桶限流
     *
     * <p>以固定速率生成令牌，请求消耗令牌。
     * 如果令牌足够，返回 true；否则返回 false。
     *
     * <p><b>算法说明：</b>
     * <ul>
     *   <li>使用缓存存储令牌数和最后补充时间</li>
     *   <li>每次请求时，计算应生成的令牌数</li>
     *   <li>如果令牌足够，消耗令牌并返回 true</li>
     * </ul>
     *
     * @param key 限流键
     * @param maxTokens 最大令牌数
     * @param refillInterval 令牌补充间隔
     * @return true 如果允许请求，false 如果令牌不足
     */
    public boolean tryAcquireToken(K key, int maxTokens, Duration refillInterval) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("Max tokens must be positive");
        }
        if (refillInterval == null || refillInterval.isNegative() || refillInterval.isZero()) {
            throw new IllegalArgumentException("Refill interval must be positive");
        }

        RateLimitInfo info = get(key);
        long now = System.currentTimeMillis();

        if (info == null) {
            // 首次请求，创建限流信息（初始令牌数为最大值）
            RateLimitInfo newInfo = new RateLimitInfo(maxTokens - 1, now, refillInterval.toMillis());
            put(key, newInfo, refillInterval.multipliedBy(2));
            return true;
        }

        // 计算应生成的令牌数
        long lastRefill = info.lastRequestTime();
        long elapsed = now - lastRefill;
        long tokensToAdd = elapsed / refillInterval.toMillis();

        // 计算新令牌数（不超过最大值）
        int currentTokens = info.count();
        long newTokens = Math.min(currentTokens + tokensToAdd, maxTokens);

        // 检查是否有足够令牌
        if (newTokens < 1) {
            return false;
        }

        // 消耗一个令牌
        newTokens--;

        // 更新限流信息
        RateLimitInfo updatedInfo = new RateLimitInfo((int) newTokens, now, refillInterval.toMillis());
        put(key, updatedInfo, refillInterval.multipliedBy(2));
        return true;
    }

    /**
     * 固定窗口限流
     *
     * <p>在固定时间窗口内统计请求数，窗口不滑动。
     * 如果请求数未超过限制，返回 true；否则返回 false。
     *
     * <p><b>算法说明：</b>
     * <ul>
     *   <li>使用原子递增操作统计请求数</li>
     *   <li>每个窗口使用独立的 key（包含窗口编号）</li>
     *   <li>窗口过期后自动重置计数</li>
     * </ul>
     *
     * @param key 限流键
     * @param maxRequests 最大请求数
     * @param window 时间窗口
     * @return true 如果允许请求，false 如果超过限制
     */
    public boolean tryAcquireFixedWindow(K key, int maxRequests, Duration window) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("Max requests must be positive");
        }
        if (window == null || window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("Window must be positive");
        }

        // 计算窗口编号
        long windowNumber = System.currentTimeMillis() / window.toMillis();
        K windowKey = createWindowKey(key, windowNumber);

        // 使用原子递增操作
        long count = getCache().increment(windowKey);

        // 设置过期时间（确保窗口过期后自动清理）
        getCache().expire(windowKey, window);

        return count <= maxRequests;
    }

    /**
     * 创建窗口键
     *
     * <p>为固定窗口限流创建包含窗口编号的键。
     * 子类可以覆盖此方法自定义窗口键格式。
     *
     * @param key 原始键
     * @param windowNumber 窗口编号
     * @return 窗口键
     */
    @SuppressWarnings("unchecked")
    protected K createWindowKey(K key, long windowNumber) {
        // 默认实现：将窗口编号追加到键后面
        // 子类可以覆盖此方法以支持自定义键类型
        if (key instanceof String) {
            return (K) (key + ":window:" + windowNumber);
        }
        // 对于非 String 类型，使用原始键（可能丢失窗口隔离）
        log.warn("Window key creation not supported for non-String key type: {}", key.getClass());
        return key;
    }

    /**
     * 获取剩余配额
     *
     * <p>获取指定 key 的剩余请求配额。
     * 注意：此方法仅适用于固定窗口限流，其他算法可能不准确。
     *
     * @param key 限流键
     * @param maxRequests 最大请求数
     * @param window 时间窗口
     * @return 剩余配额
     */
    public int getRemainingQuota(K key, int maxRequests, Duration window) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        long windowNumber = System.currentTimeMillis() / window.toMillis();
        K windowKey = createWindowKey(key, windowNumber);

        // 固定窗口限流使用原子递增，存储的是 Long 类型
        // 使用 getCache().get() 方法获取值，然后转换为 Long
        Long count = getCache().get(windowKey, Long.class);
        long currentCount = count != null ? count : 0L;
        return (int) Math.max(0, maxRequests - currentCount);
    }

    /**
     * 重置限流
     *
     * <p>重置指定 key 的限流信息，清除所有限制。
     *
     * @param key 限流键
     */
    public void reset(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        evict(key);
    }

    /**
     * 限流信息
     *
     * <p>封装限流相关的数据，包括计数、最后请求时间、窗口大小等。
     *
     * @param count 当前计数（请求数或令牌数）
     * @param lastRequestTime 最后请求时间（毫秒时间戳）
     * @param windowSize 窗口大小（毫秒）
     */
    public record RateLimitInfo(int count, long lastRequestTime, long windowSize) {
        /**
         * 构造函数
         *
         * @param count 当前计数
         * @param lastRequestTime 最后请求时间
         * @param windowSize 窗口大小
         */
        public RateLimitInfo {
            if (count < 0) {
                throw new IllegalArgumentException("Count must be non-negative");
            }
            if (lastRequestTime < 0) {
                throw new IllegalArgumentException("Last request time must be non-negative");
            }
            if (windowSize <= 0) {
                throw new IllegalArgumentException("Window size must be positive");
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RateLimitInfo that = (RateLimitInfo) o;
            return count == that.count
                    && lastRequestTime == that.lastRequestTime
                    && windowSize == that.windowSize;
        }

        @Override
        public int hashCode() {
            return Objects.hash(count, lastRequestTime, windowSize);
        }
    }

}
