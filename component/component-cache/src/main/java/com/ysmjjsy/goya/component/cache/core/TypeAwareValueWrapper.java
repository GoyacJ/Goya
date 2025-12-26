package com.ysmjjsy.goya.component.cache.core;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/26 16:20
 */
public class TypeAwareValueWrapper implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 缓存值
     */
    private final Object value;

    /**
     * 值的类型全限定名
     */
    private final String typeName;

    /**
     * 构造函数
     *
     * @param value 缓存值
     * @param typeName 值的类型全限定名（如 "java.lang.String"）
     * @throws IllegalArgumentException 如果 value 或 typeName 为 null
     */
    public TypeAwareValueWrapper(Object value, String typeName) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (typeName == null || typeName.isEmpty()) {
            throw new IllegalArgumentException("Type name cannot be null or empty");
        }
        this.value = value;
        this.typeName = typeName;
    }

    /**
     * 从值自动创建包装器
     *
     * <p>自动获取值的类型信息并创建包装器。
     *
     * @param value 缓存值
     * @return TypeAwareValueWrapper 实例
     * @throws IllegalArgumentException 如果 value 为 null
     */
    public static TypeAwareValueWrapper fromValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        // 如果已经是包装器，直接返回
        if (value instanceof TypeAwareValueWrapper) {
            return (TypeAwareValueWrapper) value;
        }
        // 获取值的类型全限定名
        String typeName = value.getClass().getName();
        return new TypeAwareValueWrapper(value, typeName);
    }

    /**
     * 获取缓存值
     *
     * @return 缓存值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 获取类型全限定名
     *
     * @return 类型全限定名
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * 获取类型 Class 对象
     *
     * @return 类型 Class 对象
     * @throws ClassNotFoundException 如果类型不存在
     */
    public Class<?> getType() throws ClassNotFoundException {
        return Class.forName(typeName);
    }

    @Override
    public String toString() {
        return "TypeAwareValueWrapper{" +
                "typeName='" + typeName + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeAwareValueWrapper that = (TypeAwareValueWrapper) o;

        if (!value.equals(that.value)) return false;
        return typeName.equals(that.typeName);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + typeName.hashCode();
        return result;
    }
}
