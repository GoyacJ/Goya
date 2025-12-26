package com.ysmjjsy.goya.component.cache.local;

import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Null 值包装器
 *
 * <p>用于在多级缓存中安全地表示 null 值，避免与用户对象类型冲突。
 * 使用特殊标记（MAGIC_BYTES）和版本号确保序列化兼容性。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>区分"缓存中不存在"和"缓存了 null 值"</li>
 *   <li>避免与用户可能缓存的 NullValueWrapper 类型对象冲突</li>
 *   <li>支持跨节点序列化（Redis）</li>
 *   <li>支持版本升级时的兼容性检查</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>当 {@link CacheSpecification#isAllowNullValues()} 为 true 时</li>
 *   <li>用户方法返回 null 时，使用此包装器存储到缓存</li>
 *   <li>从缓存读取时，识别此包装器并返回 null 给调用方</li>
 * </ul>
 *
 * <p><b>序列化安全：</b>
 * <ul>
 *   <li>实现 {@link Serializable} 接口</li>
 *   <li>使用 readResolve() 确保反序列化后返回单例</li>
 *   <li>包含版本号用于未来兼容性检查</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:54
 */
public final class NullValueWrapper implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 魔法字节数组，用于识别 NullValueWrapper
     * 使用固定字符串确保跨序列化框架的一致性
     */
    private static final byte[] MAGIC_BYTES = "GOYA_NULL_V1".getBytes(StandardCharsets.UTF_8);

    /**
     * 单例实例
     */
    public static final NullValueWrapper INSTANCE = new NullValueWrapper();

    /**
     * 魔法字节数组（实例字段，用于序列化）
     */
    private final byte[] magic = MAGIC_BYTES;

    /**
     * 版本号，用于未来兼容性检查
     * -- GETTER --
     *  获取版本号
     *
     */
    @Getter
    private static final int VERSION = 1;

    /**
     * 私有构造函数，确保单例
     */
    private NullValueWrapper() {
        // 单例模式
    }

    /**
     * 检查对象是否为 NullValueWrapper 实例
     *
     * <p>用于从缓存读取时判断是否需要解包为 null。
     *
     * @param obj 待检查的对象
     * @return 如果 obj 是 NullValueWrapper 实例，返回 true；否则返回 false
     */
    public static boolean isNullValue(Object obj) {
        return obj instanceof NullValueWrapper;
    }

    /**
     * 反序列化时返回单例实例
     *
     * <p>确保无论序列化多少次，反序列化后都返回同一个实例。
     * 这是 Java 序列化机制的标准模式。
     *
     * @return 单例实例
     */
    private Object readResolve() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "NullValueWrapper{version=" + VERSION + "}";
    }
}
