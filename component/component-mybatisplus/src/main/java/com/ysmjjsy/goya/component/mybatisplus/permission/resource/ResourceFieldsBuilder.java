package com.ysmjjsy.goya.component.mybatisplus.permission.resource;

import java.util.Objects;

/**
 * <p>单个资源的字段注册器</p>
 * <p>
 * 用于在 {@link ResourceRegistryBuilder#resource(String, java.util.function.Consumer)} 内声明：
 * <ul>
 *   <li>资源与主表映射</li>
 *   <li>资源字段白名单</li>
 *   <li>资源与 Mapper 方法（msId）的精确绑定</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/29 00:33
 */
public class ResourceFieldsBuilder {

    private final String resource;
    private final ResourceRegistryBuilder parent;

    ResourceFieldsBuilder(String resource, ResourceRegistryBuilder parent) {
        this.resource = Objects.requireNonNull(resource, "resource 不能为空");
        this.parent = Objects.requireNonNull(parent, "parent 不能为空");
    }

    /**
     * 为该资源注册一个“兜底表映射”。
     * <p>
     * 当 msId 未命中时，若 SQL 表名命中该表，则认为属于该资源。
     *
     * @param tableName 表名（建议填主表）
     * @return this
     */
    public ResourceFieldsBuilder table(String tableName) {
        parent.mapTable(tableName, resource);
        return this;
    }

    /**
     * 为该资源注册一个 Mapper 方法映射（msId → resource）。
     *
     * @param mappedStatementId MyBatis mappedStatementId
     * @return this
     */
    public ResourceFieldsBuilder ms(String mappedStatementId) {
        parent.mapStatement(mappedStatementId, resource);
        return this;
    }

    /**
     * 注册字段白名单：fieldKey → 列引用。
     *
     * @param fieldKey 规则侧字段 key（如 deptId）
     * @param table 表名（建议填主表名）
     * @param column 列名（如 dept_id）
     * @return this
     */
    public ResourceFieldsBuilder field(String fieldKey, String table, String column) {
        parent.mapField(resource, fieldKey, table, column);
        return this;
    }
}
