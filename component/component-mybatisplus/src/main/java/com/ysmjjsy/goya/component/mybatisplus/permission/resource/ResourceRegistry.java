package com.ysmjjsy.goya.component.mybatisplus.permission.resource;

/**
 * <p>资源注册表</p>
 * <p>
 * 用于在 SQL 执行阶段，将 table / mappedStatementId
 * 映射为“逻辑资源”，并进一步将字段 key 映射为安全列。
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>table + msId → resource</li>
 *   <li>resource + fieldKey → ColumnRef（白名单）</li>
 * </ul>
 *
 * <p><b>重要约束：</b>
 * <ul>
 *   <li>用户规则只能使用 resource + fieldKey</li>
 *   <li>禁止用户规则直接声明 column</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 22:27
 */
public interface ResourceRegistry {

    /**
     * 解析逻辑资源。
     *
     * @param tableName 表名
     * @param mappedStatementId MyBatis MappedStatement Id
     * @return resource；无法解析返回 null
     */
    String resolveResource(String tableName, String mappedStatementId);


    ColumnRef resolveColumn(String resource, String fieldKey);
}