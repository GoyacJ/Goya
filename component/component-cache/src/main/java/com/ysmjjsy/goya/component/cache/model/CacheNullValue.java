package com.ysmjjsy.goya.component.cache.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>缓存空值哨兵</p>
 * <p>用于解决缓存穿透问题：将"不存在"的查询结果缓存为哨兵值，避免直接写入 null</p>
 *
 * <p>设计理由：</p>
 * <ul>
 *     <li>Caffeine / Redis 都支持存储非 null 对象</li>
 *     <li>使用哨兵值比直接存 null 更安全，避免 API 语义歧义</li>
 *     <li>短 TTL 确保不会长期占用缓存空间</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>数据库查询结果为 null 时，缓存哨兵值</li>
 *     <li>读取时检测到哨兵值，返回 null 给调用方</li>
 *     <li>防止高频查询不存在的 key 打到数据库（缓存穿透）</li>
 * </ul>
 *
 * <p>实现为单例，节省内存</p>
 *
 * @author goya
 * @since 2025/12/23
 * @see java.io.Serializable
 */
public final class CacheNullValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 单例实例
     */
    public static final CacheNullValue INSTANCE = new CacheNullValue();

    /**
     * 私有构造器，禁止外部实例化
     */
    private CacheNullValue() {
        // 禁止反射创建实例
        if (INSTANCE != null) {
            throw new IllegalStateException("CacheNullValue is a singleton");
        }
    }

    /**
     * 反序列化时返回单例实例
     */
    @Serial
    private Object readResolve() {
        return INSTANCE;
    }

    /**
     * 判断给定值是否为空值哨兵
     *
     * @param value 待判断的值
     * @return true 表示是空值哨兵，false 表示不是
     */
    public static boolean isNullValue(Object value) {
        return value instanceof CacheNullValue;
    }

    @Override
    public String toString() {
        return "CacheNullValue";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CacheNullValue;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}

