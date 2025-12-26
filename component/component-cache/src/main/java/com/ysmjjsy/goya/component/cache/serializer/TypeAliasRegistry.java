package com.ysmjjsy.goya.component.cache.serializer;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类型别名注册表
 *
 * <p>管理类型别名与全限定类名的映射关系，解决序列化兼容性问题。
 * 支持类重构、包名迁移、灰度发布等场景下的类型兼容。
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>解决类重构导致的序列化兼容性问题（如 com.example.User → com.example.model.User）</li>
 *   <li>支持包名迁移（如 com.company.old.User → com.company.new.User）</li>
 *   <li>支持灰度发布下的类版本兼容</li>
 * </ul>
 *
 * <p><b>工作原理：</b>
 * <ol>
 *   <li>注册别名：将短别名（如 "User"）映射到全限定类名（如 "com.example.User"）</li>
 *   <li>注册迁移：将旧类名映射到新类名，支持反向查找</li>
 *   <li>解析类名：优先使用别名，失败时尝试迁移映射</li>
 * </ol>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>使用 {@link ConcurrentHashMap} 存储映射关系，线程安全</li>
 *   <li>支持并发注册和查询</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26
 */
@Slf4j
public class TypeAliasRegistry {

    /**
     * 别名到类名的映射
     * Key: 别名（如 "User"）
     * Value: 全限定类名（如 "com.example.User"）
     */
    private final Map<String, String> aliasToClassName = new ConcurrentHashMap<>();

    /**
     * 类名到别名的映射（反向查找）
     * Key: 全限定类名
     * Value: 别名列表（一个类可以有多个别名）
     */
    private final Map<String, List<String>> classNameToAliases = new ConcurrentHashMap<>();

    /**
     * 类名迁移映射（旧类名 → 新类名）
     * Key: 旧类名
     * Value: 新类名
     */
    private final Map<String, String> classNameMigration = new ConcurrentHashMap<>();

    /**
     * 注册类型别名
     *
     * <p>将短别名映射到全限定类名。序列化时可以使用别名，反序列化时自动解析为全限定类名。
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>类重构：注册新类名，保留旧类名作为别名</li>
     *   <li>简化序列化：使用短别名减少序列化数据大小</li>
     * </ul>
     *
     * @param alias 别名（如 "User"）
     * @param className 全限定类名（如 "com.example.User"）
     * @throws IllegalArgumentException 如果 alias 或 className 为 null
     */
    public void registerAlias(String alias, String className) {
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("Alias cannot be null or empty");
        }
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("ClassName cannot be null or empty");
        }

        aliasToClassName.put(alias, className);
        classNameToAliases.computeIfAbsent(className, k -> new ArrayList<>()).add(alias);

        if (log.isDebugEnabled()) {
            log.debug("Registered type alias: {} -> {}", alias, className);
        }
    }

    /**
     * 注册类名迁移
     *
     * <p>将旧类名映射到新类名，支持反向查找。反序列化时，如果旧类名不存在，自动使用新类名。
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>包名迁移：com.company.old.User → com.company.new.User</li>
     *   <li>类重构：com.example.User → com.example.model.User</li>
     * </ul>
     *
     * @param oldClassName 旧类名
     * @param newClassName 新类名
     * @throws IllegalArgumentException 如果 oldClassName 或 newClassName 为 null
     */
    public void registerMigration(String oldClassName, String newClassName) {
        if (oldClassName == null || oldClassName.isEmpty()) {
            throw new IllegalArgumentException("OldClassName cannot be null or empty");
        }
        if (newClassName == null || newClassName.isEmpty()) {
            throw new IllegalArgumentException("NewClassName cannot be null or empty");
        }

        classNameMigration.put(oldClassName, newClassName);

        if (log.isDebugEnabled()) {
            log.debug("Registered class name migration: {} -> {}", oldClassName, newClassName);
        }
    }

    /**
     * 解析类名（从别名或迁移映射）
     *
     * <p>优先使用别名解析，如果失败，尝试从迁移映射解析。
     *
     * @param aliasOrClassName 别名或类名
     * @return 解析后的全限定类名，如果无法解析则返回原值
     */
    public String resolveClassName(String aliasOrClassName) {
        if (aliasOrClassName == null || aliasOrClassName.isEmpty()) {
            return aliasOrClassName;
        }

        // 1. 尝试从别名解析
        String className = aliasToClassName.get(aliasOrClassName);
        if (className != null) {
            return className;
        }

        // 2. 尝试从迁移映射解析
        className = classNameMigration.get(aliasOrClassName);
        if (className != null) {
            return className;
        }

        // 3. 如果都不是，返回原值（可能是全限定类名）
        return aliasOrClassName;
    }

    /**
     * 解析类（从别名或迁移映射）
     *
     * <p>解析类名并加载类。如果解析失败，尝试从迁移映射反向查找。
     *
     * @param aliasOrClassName 别名或类名
     * @return 解析后的 Class 对象
     * @throws ClassNotFoundException 如果类无法找到
     */
    public Class<?> resolveClass(String aliasOrClassName) throws ClassNotFoundException {
        if (aliasOrClassName == null || aliasOrClassName.isEmpty()) {
            throw new IllegalArgumentException("AliasOrClassName cannot be null or empty");
        }

        // 1. 解析类名
        String className = resolveClassName(aliasOrClassName);

        // 2. 尝试加载类
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            // 3. 如果失败，尝试从迁移映射反向查找
            for (Map.Entry<String, String> entry : classNameMigration.entrySet()) {
                if (entry.getValue().equals(aliasOrClassName)) {
                    // 找到了迁移映射，尝试加载新类名
                    try {
                        return Class.forName(entry.getKey());
                    } catch (ClassNotFoundException ignored) {
                        // 继续查找
                    }
                }
            }

            // 4. 如果都失败，尝试从别名反向查找
            for (Map.Entry<String, List<String>> entry : classNameToAliases.entrySet()) {
                if (entry.getValue().contains(aliasOrClassName)) {
                    try {
                        return Class.forName(entry.getKey());
                    } catch (ClassNotFoundException ignored) {
                        // 继续查找
                    }
                }
            }

            // 5. 所有尝试都失败，抛出异常
            throw new ClassNotFoundException("Class not found: " + aliasOrClassName + " (resolved to: " + className + ")");
        }
    }

    /**
     * 检查别名是否存在
     *
     * @param alias 别名
     * @return true 如果别名已注册
     */
    public boolean hasAlias(String alias) {
        return alias != null && aliasToClassName.containsKey(alias);
    }

    /**
     * 检查迁移映射是否存在
     *
     * @param oldClassName 旧类名
     * @return true 如果迁移映射已注册
     */
    public boolean hasMigration(String oldClassName) {
        return oldClassName != null && classNameMigration.containsKey(oldClassName);
    }

    /**
     * 清除所有注册的别名和迁移映射（用于测试或重置）
     */
    public void clear() {
        aliasToClassName.clear();
        classNameToAliases.clear();
        classNameMigration.clear();
        if (log.isDebugEnabled()) {
            log.debug("Cleared all type aliases and migrations");
        }
    }
}

