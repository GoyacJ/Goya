package com.ysmjjsy.goya.component.cache.multilevel.ttl;

import java.time.Duration;

/**
 * L1 TTL 计算策略接口
 *
 * <p>定义如何根据 L2 TTL 计算 L1 TTL，支持多种策略以满足不同业务场景的一致性要求。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>避免硬编码 L1 TTL 为 L2 TTL 的固定比例（如 80%）</li>
 *   <li>支持不同业务场景的灵活性配置</li>
 *   <li>支持未来扩展更复杂的策略（如基于访问频率的动态 TTL）</li>
 * </ul>
 *
 * <p><b>策略选择原则：</b>
 * <ul>
 *   <li><b>固定比例策略</b>：适用于大多数场景，L1 稍短于 L2，确保最终一致性</li>
 *   <li><b>固定时间策略</b>：适用于 L1 需要固定过期时间的场景</li>
 *   <li><b>独立配置策略</b>：适用于 L1 和 L2 需要完全独立 TTL 的场景</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:25
 */
public interface TtlStrategy {

    /**
     * 根据 L2 TTL 计算 L1 TTL
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>根据策略类型计算 L1 TTL</li>
     *   <li>确保返回的 TTL 大于 0</li>
     *   <li>返回计算结果</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果 l2Ttl 为 null，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果 l2Ttl 小于等于 0，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果计算结果小于等于 0，抛出 {@link IllegalStateException}</li>
     * </ul>
     *
     * @param l2Ttl L2 缓存的 TTL，必须大于 0
     * @return L1 缓存的 TTL，必须大于 0
     * @throws IllegalArgumentException 如果 l2Ttl 无效
     * @throws IllegalStateException    如果计算结果无效
     */
    Duration calculateLocalTtl(Duration l2Ttl);

    /**
     * 固定比例策略
     *
     * <p>L1 TTL = L2 TTL × ratio
     *
     * <p><b>适用场景：</b>
     * <ul>
     *   <li>大多数业务场景的默认选择</li>
     *   <li>ratio 通常设置为 0.8（80%），确保 L1 先于 L2 过期</li>
     *   <li>更高的一致性要求可以使用 0.9（90%）</li>
     * </ul>
     *
     * <p><b>示例：</b>
     * <pre>
     * L2 TTL = 1 小时，ratio = 0.8
     * L1 TTL = 1 小时 × 0.8 = 48 分钟
     * </pre>
     *
     * @param ratio -- GETTER --
     *              获取比例
     */
    record FixedRatioStrategy(double ratio) implements TtlStrategy {

        /**
         * 创建固定比例策略
         *
         * @param ratio 比例，必须在 (0, 1] 范围内，通常为 0.8 或 0.9
         * @throws IllegalArgumentException 如果 ratio 不在有效范围内
         */
        public FixedRatioStrategy {
            if (ratio <= 0 || ratio > 1) {
                throw new IllegalArgumentException("Ratio must be in (0, 1], got: " + ratio);
            }
        }

        @Override
        public Duration calculateLocalTtl(Duration l2Ttl) {
            if (l2Ttl == null || l2Ttl.isNegative() || l2Ttl.isZero()) {
                throw new IllegalArgumentException("L2 TTL must be positive, got: " + l2Ttl);
            }
            long millis = (long) (l2Ttl.toMillis() * ratio);
            if (millis <= 0) {
                throw new IllegalStateException("Calculated L1 TTL is not positive: " + millis + "ms");
            }
            return Duration.ofMillis(millis);
        }

    }

    /**
     * 固定时间策略
     *
     * <p>L1 TTL = fixedDuration（与 L2 TTL 无关）
     *
     * <p><b>适用场景：</b>
     * <ul>
     *   <li>L1 需要固定过期时间，不受 L2 TTL 影响</li>
     *   <li>例如：L1 固定 5 分钟，L2 可能是 1 小时或 2 小时</li>
     * </ul>
     *
     * <p><b>示例：</b>
     * <pre>
     * fixedDuration = 5 分钟
     * 无论 L2 TTL 是多少，L1 TTL 都是 5 分钟
     * </pre>
     *
     * @param fixedDuration -- GETTER --
     *                      获取固定时间
     */
    record FixedDurationStrategy(Duration fixedDuration) implements TtlStrategy {

        /**
         * 创建固定时间策略
         *
         * @param fixedDuration 固定的 L1 TTL，必须大于 0
         * @throws IllegalArgumentException 如果 fixedDuration 无效
         */
        public FixedDurationStrategy {
            if (fixedDuration == null || fixedDuration.isNegative() || fixedDuration.isZero()) {
                throw new IllegalArgumentException("Fixed duration must be positive, got: " + fixedDuration);
            }
        }

        @Override
        public Duration calculateLocalTtl(Duration l2Ttl) {
            // 忽略 l2Ttl，直接返回固定时间
            return fixedDuration;
        }

    }

    /**
     * 独立配置策略
     *
     * <p>L1 TTL = localTtl（与 L2 TTL 完全独立）
     *
     * <p><b>适用场景：</b>
     * <ul>
     *   <li>L1 和 L2 需要完全独立的 TTL 配置</li>
     *   <li>例如：L1 = 5 分钟，L2 = 2 小时</li>
     * </ul>
     *
     * <p><b>注意：</b>此策略可能导致 L1 过期后 L2 仍有效，需要业务方接受这种不一致性。
     *
     * <p><b>示例：</b>
     * <pre>
     * localTtl = 5 分钟
     * 无论 L2 TTL 是多少，L1 TTL 都是 5 分钟
     * </pre>
     *
     * @param localTtl -- GETTER --
     *                 获取独立配置的 TTL
     */
    record IndependentStrategy(Duration localTtl) implements TtlStrategy {

        /**
         * 创建独立配置策略
         *
         * @param localTtl L1 的独立 TTL，必须大于 0
         * @throws IllegalArgumentException 如果 localTtl 无效
         */
        public IndependentStrategy {
            if (localTtl == null || localTtl.isNegative() || localTtl.isZero()) {
                throw new IllegalArgumentException("Local TTL must be positive, got: " + localTtl);
            }
        }

        @Override
        public Duration calculateLocalTtl(Duration l2Ttl) {
            // 忽略 l2Ttl，直接返回独立配置的 TTL
            return localTtl;
        }

    }
}
