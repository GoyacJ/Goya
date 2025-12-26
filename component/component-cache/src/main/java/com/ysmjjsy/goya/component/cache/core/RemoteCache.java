package com.ysmjjsy.goya.component.cache.core;

import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 远程缓存抽象接口
 *
 * <p>扩展 Spring Cache 的 {@link Cache} 接口，为多级缓存编排提供远程缓存（L2）能力。
 * 远程缓存通常使用分布式存储，如 Redis，提供跨节点的数据共享。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>提供远程缓存的读写操作</li>
 *   <li>支持 TTL 配置的写入操作</li>
 *   <li>支持批量操作以提升性能</li>
 *   <li>支持异步操作以降低延迟</li>
 * </ul>
 *
 * <p><b>与 Spring Cache 的集成点：</b>
 * <ul>
 *   <li>继承 {@link Cache} 接口，完全兼容 Spring Cache SPI</li>
 *   <li>实现类将被 {@link GoyaCache} 使用，作为 L2 缓存层</li>
 * </ul>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>所有方法必须是线程安全的</li>
 *   <li>实现类应支持高并发读写操作</li>
 *   <li>异步方法应使用合适的线程池执行</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:44
 */
public interface RemoteCache extends Cache {

    /**
     * 写入缓存，指定 TTL
     *
     * <p>将键值对写入远程缓存，并设置过期时间。如果 key 已存在，将覆盖旧值。
     * 此操作是同步的，确保写入完成后才返回。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>序列化 value（如果 value 为 NullValueWrapper，需要正确序列化）</li>
     *   <li>构建远程缓存 key（包含 cacheName 前缀）</li>
     *   <li>调用远程缓存 API 写入数据</li>
     *   <li>等待写入完成（同步操作）</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果 key 为 null，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果 TTL 为 null 或负数，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果网络异常，抛出 {@link RuntimeException}（包装原始异常）</li>
     *   <li>如果序列化失败，抛出 {@link RuntimeException}</li>
     * </ul>
     *
     * @param key 缓存键，不能为 null
     * @param value 缓存值，可以为 null（使用 NullValueWrapper 包装）
     * @param ttl 过期时间，必须大于 0
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException 如果远程缓存操作失败
     */
    void put(Object key, Object value, Duration ttl);

    /**
     * 批量获取缓存值
     *
     * <p>一次性获取多个 key 的缓存值，减少网络往返次数。对于未命中的 key，返回的 Map 中不包含该 key。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>构建批量查询请求（如 Redis MGET）</li>
     *   <li>发送请求到远程缓存</li>
     *   <li>反序列化返回结果</li>
     *   <li>构建结果 Map（只包含命中的 key）</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果 keys 为 null，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果 keys 为空，返回空 Map（不抛出异常）</li>
     *   <li>如果网络异常，抛出 {@link RuntimeException}</li>
     *   <li>如果部分 key 查询失败，记录警告日志，返回已成功查询的结果</li>
     * </ul>
     *
     * @param keys 缓存键集合，不能为 null
     * @return 命中的 key-value 映射，key 为缓存键，value 为 ValueWrapper，不会为 null，但可能为空 Map
     * @throws IllegalArgumentException 如果 keys 为 null
     * @throws RuntimeException 如果批量查询完全失败
     */
    Map<Object, ValueWrapper> getAll(Set<Object> keys);

    /**
     * 批量写入缓存
     *
     * <p>一次性写入多个键值对，减少网络往返次数。所有条目使用相同的 TTL。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>序列化所有 value</li>
     *   <li>构建批量写入请求（如 Redis Pipeline）</li>
     *   <li>发送请求到远程缓存</li>
     *   <li>等待所有写入完成</li>
     * </ol>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果 entries 为 null，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果 entries 为空，直接返回（不抛出异常）</li>
     *   <li>如果 TTL 无效，抛出 {@link IllegalArgumentException}</li>
     *   <li>如果部分 key 写入失败，记录警告日志但不中断操作</li>
     * </ul>
     *
     * @param entries 键值对映射，不能为 null
     * @param ttl 过期时间，必须大于 0，所有条目共享此 TTL
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException 如果批量写入完全失败
     */
    void putAll(Map<Object, Object> entries, Duration ttl);

    /**
     * 异步获取缓存值
     *
     * <p>异步方式获取缓存值，不阻塞调用线程。适用于对延迟敏感的场景。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>提交异步查询任务到线程池</li>
     *   <li>立即返回 CompletableFuture</li>
     *   <li>后台线程执行查询操作</li>
     *   <li>查询完成后完成 Future</li>
     * </ol>
     *
     * <p><b>线程模型：</b>
     * <ul>
     *   <li>查询操作在独立的线程中执行</li>
     *   <li>调用线程不会被阻塞</li>
     *   <li>Future 的完成可能在任意线程</li>
     * </ul>
     *
     * <p><b>异常处理：</b>
     * <ul>
     *   <li>如果 key 为 null，Future 将异常完成（IllegalArgumentException）</li>
     *   <li>如果网络异常，Future 将异常完成（RuntimeException）</li>
     *   <li>如果未命中，Future 正常完成，返回 null</li>
     * </ul>
     *
     * <p><b>注意：</b>此方法是可选的，实现类可以不支持异步操作，此时应抛出 {@link UnsupportedOperationException}。
     *
     * @param key 缓存键，不能为 null
     * @return CompletableFuture，正常完成时包含 ValueWrapper（未命中时为 null），异常完成时包含异常
     * @throws UnsupportedOperationException 如果实现类不支持异步操作
     */
    default CompletableFuture<ValueWrapper> getAsync(Object key) {
        throw new UnsupportedOperationException("Async operations are not supported by this implementation");
    }
}