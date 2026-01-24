package com.ysmjjsy.goya.component.framework.core.enums.dict;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * <p>枚举字典注册表：保存“可导出”的 CodeEnum 枚举类型索引</p>
 *
 * <p>该注册表由扫描器构建（默认扫描 Spring Boot 应用基础包），用于：</p>
 * <ul>
 *   <li>列出所有可导出枚举</li>
 *   <li>根据枚举名解析对应枚举类型</li>
 *   <li>处理 simpleName 冲突（同名不同包）</li>
 * </ul>
 *
 * <p><b>安全原则：</b>只有扫描到的枚举才允许导出。</p>
 *
 * @author goya
 * @since 2026/1/24 16:00
 */
public class EnumDictionaryRegistry {


    /**
     * key：全限定名 -> 枚举类型
     */
    private final Map<String, Class<? extends Enum<?>>> byFullName;

    /**
     * key：简单名 -> 枚举类型（仅当简单名唯一时才存在）
     */
    private final Map<String, Class<? extends Enum<?>>> bySimpleName;

    /**
     * key：简单名 -> 冲突的全限定名列表（当发生冲突时记录）
     */
    private final Map<String, List<String>> simpleNameConflicts;

    /**
     * 构造注册表。
     *
     * @param enumTypes 扫描到的枚举类型集合
     */
    public EnumDictionaryRegistry(Collection<Class<? extends Enum<?>>> enumTypes) {
        Map<String, Class<? extends Enum<?>>> full = new LinkedHashMap<>();
        Map<String, List<String>> simpleToFullNames = new LinkedHashMap<>();

        if (enumTypes != null) {
            for (Class<? extends Enum<?>> e : enumTypes) {
                if (e == null) {
                    continue;
                }
                if (!e.isEnum() || !CodeEnum.class.isAssignableFrom(e)) {
                    continue;
                }
                full.putIfAbsent(e.getName(), e);

                String simple = e.getSimpleName();
                simpleToFullNames.computeIfAbsent(simple, k -> new ArrayList<>()).add(e.getName());
            }
        }

        Map<String, Class<? extends Enum<?>>> simpleUnique = new LinkedHashMap<>();
        Map<String, List<String>> conflicts = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : simpleToFullNames.entrySet()) {
            String simple = entry.getKey();
            List<String> fullNames = entry.getValue();
            if (fullNames.size() == 1) {
                String only = fullNames.get(0);
                simpleUnique.put(simple, full.get(only));
            } else {
                conflicts.put(simple, List.copyOf(fullNames));
            }
        }

        this.byFullName = Collections.unmodifiableMap(full);
        this.bySimpleName = Collections.unmodifiableMap(simpleUnique);
        this.simpleNameConflicts = Collections.unmodifiableMap(conflicts);
    }

    /**
     * 列出所有可导出枚举（simpleName -> fullName）。
     *
     * <p>若 simpleName 冲突，则该 simpleName 不会出现在返回 map 中（避免歧义）。</p>
     *
     * @return 可导出枚举列表
     */
    public Map<String, String> listExportableEnums() {
        Map<String, String> out = new TreeMap<>();
        for (Map.Entry<String, Class<? extends Enum<?>>> e : bySimpleName.entrySet()) {
            out.put(e.getKey(), e.getValue().getName());
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * 获取 simpleName 冲突列表（simpleName -> 冲突的 fullName 列表）。
     *
     * @return 冲突信息
     */
    public Map<String, List<String>> conflicts() {
        return simpleNameConflicts;
    }

    /**
     * 根据枚举名解析枚举类型。
     *
     * <p>支持：</p>
     * <ul>
     *   <li>简单名：UserStatus（要求无冲突）</li>
     *   <li>全限定名：com.xxx.UserStatus</li>
     * </ul>
     *
     * @param enumName 枚举名
     * @return 枚举类型
     */
    public Class<? extends Enum<?>> resolve(String enumName) {
        if (!StringUtils.hasText(enumName)) {
            throw new IllegalArgumentException("enumName 不能为空");
        }

        // 1) 全限定名优先
        Class<? extends Enum<?>> byFull = byFullName.get(enumName);
        if (byFull != null) {
            return byFull;
        }

        // 2) 简单名（必须唯一）
        Class<? extends Enum<?>> bySimple = bySimpleName.get(enumName);
        if (bySimple != null) {
            return bySimple;
        }

        // 3) 简单名冲突提示
        List<String> conflicts = simpleNameConflicts.get(enumName);
        if (conflicts != null && !conflicts.isEmpty()) {
            throw new IllegalArgumentException("枚举名存在冲突：" + enumName + "，请使用全限定名之一：" + conflicts);
        }

        throw new IllegalArgumentException("未找到可导出枚举：" + enumName);
    }
}
