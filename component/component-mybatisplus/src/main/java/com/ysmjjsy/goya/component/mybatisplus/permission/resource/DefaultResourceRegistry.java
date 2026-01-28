package com.ysmjjsy.goya.component.mybatisplus.permission.resource;

import java.util.*;

/**
 * <p>默认资源注册表实现</p>
 * <p>
 * 通过显式注册来完成资源映射：
 * <ul>
 *   <li>tableName + mappedStatementId -> resource</li>
 *   <li>resource + fieldKey -> ColumnRef</li>
 * </ul>
 *
 * <h2>注册策略（推荐优先级）</h2>
 * <ol>
 *   <li>若存在 "msId 精确映射"：直接使用</li>
 *   <li>否则若存在 "tableName 映射"：使用 tableName 对应 resource</li>
 *   <li>否则返回 null（表示该 SQL 不纳入数据权限治理）</li>
 * </ol>
 *
 * <h2>安全约束</h2>
 * <ul>
 *   <li>fieldKey 只能映射到 ColumnRef（白名单）</li>
 *   <li>column/table 必须通过 {@link ColumnNameValidator} 校验</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 23:07
 */
public class DefaultResourceRegistry implements ResourceRegistry {

    private final Map<String, String> msIdToResource;
    private final Map<String, String> tableToResource;
    private final Map<ResourceFieldKey, ColumnRef> fieldMapping;

    private final ColumnNameValidator validator;

    public DefaultResourceRegistry(Map<String, String> msIdToResource,
                                   Map<String, String> tableToResource,
                                   Map<ResourceFieldKey, ColumnRef> fieldMapping,
                                   ColumnNameValidator validator) {
        this.msIdToResource = Map.copyOf(Objects.requireNonNull(msIdToResource, "msIdToResource 不能为空"));
        this.tableToResource = Map.copyOf(Objects.requireNonNull(tableToResource, "tableToResource 不能为空"));
        this.fieldMapping = Map.copyOf(Objects.requireNonNull(fieldMapping, "fieldMapping 不能为空"));
        this.validator = validator == null ? new ColumnNameValidator() : validator;
    }

    /**
     * 解析逻辑资源。
     *
     * @param tableName 表名
     * @param mappedStatementId MappedStatement Id
     * @return resource；无法解析返回 null（表示不追加权限条件）
     */
    @Override
    public String resolveResource(String tableName, String mappedStatementId) {
        if (mappedStatementId != null) {
            String byMs = msIdToResource.get(mappedStatementId);
            if (byMs != null) {
                return byMs;
            }
        }
        if (tableName != null) {
            return tableToResource.get(normalize(tableName));
        }
        return null;
    }

    /**
     * 将 resource + fieldKey 映射为安全列引用。
     *
     * @param resource 逻辑资源
     * @param fieldKey 字段 key（来自用户规则）
     * @return ColumnRef
     * @throws IllegalArgumentException 若映射不存在或列名非法
     */
    @Override
    public ColumnRef resolveColumn(String resource, String fieldKey) {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("resource 不能为空");
        }
        if (fieldKey == null || fieldKey.isBlank()) {
            throw new IllegalArgumentException("fieldKey 不能为空");
        }

        ColumnRef ref = fieldMapping.get(new ResourceFieldKey(resource, fieldKey));
        if (ref == null) {
            throw new IllegalArgumentException("字段未注册到白名单：resource=" + resource + " fieldKey=" + fieldKey);
        }

        // 强校验（防注入）
        validator.validateTable(ref.table());
        validator.validate(ref.column());

        return ref;
    }

    private static String normalize(String tableName) {
        // 统一小写，避免大小写导致映射失败（MySQL 通常不敏感，PG 通常敏感）
        return tableName.toLowerCase(Locale.ROOT);
    }

    /**
     * resource + fieldKey 组合 key。
     *
     * @param resource 资源
     * @param fieldKey 字段 key
     */
    public record ResourceFieldKey(String resource, String fieldKey) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ResourceFieldKey(String resource1, String key))) {
                return false;
            }
            return Objects.equals(resource(), resource1) && Objects.equals(fieldKey(), key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resource(), fieldKey());
        }
    }
}