package com.ysmjjsy.goya.component.cache.multilevel.resolver;

/**
 * 缓存配置规范解析器
 *
 * <p>根据 cacheName 解析对应的 CacheSpecification。
 * 支持按 cacheName 的特定配置，未配置项使用默认值。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>解析 cacheName 对应的 CacheSpecification</li>
 *   <li>合并默认配置和特定配置</li>
 *   <li>缓存解析结果（可选，提升性能）</li>
 * </ul>
 *
 * <p><b>配置查找顺序：</b>
 * <ol>
 *   <li>查找 cacheName 的特定配置（caches.{cacheName}）</li>
 *   <li>如果不存在，使用默认配置（default）</li>
 *   <li>如果 default 也不存在，使用硬编码默认值</li>
 * </ol>
 *
 * <p><b>配置合并策略：</b>
 * <ul>
 *   <li>特定配置覆盖默认配置</li>
 *   <li>未配置项使用默认值</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:28
 */
public interface CacheSpecificationResolver {

    /**
     * 解析 cacheName 对应的 CacheSpecification
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>查找 cacheName 的特定配置</li>
     *   <li>如果不存在，使用默认配置</li>
     *   <li>合并配置并创建 CacheSpecification 实例</li>
     *   <li>返回结果</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果 cacheName 为 null，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果配置无效，抛出 {@link IllegalStateException}</li>
     * </ul>
     *
     * @param cacheName 缓存名称
     * @return CacheSpecification 实例，不会为 null
     * @throws IllegalArgumentException 如果 cacheName 为 null
     * @throws IllegalStateException 如果配置无效
     */
    CacheSpecification resolve(String cacheName);
}

