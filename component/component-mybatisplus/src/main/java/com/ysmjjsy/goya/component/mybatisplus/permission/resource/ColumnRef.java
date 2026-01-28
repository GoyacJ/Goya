package com.ysmjjsy.goya.component.mybatisplus.permission.resource;

/**
 * <p>资源字段到物理列的安全映射</p>
 * <p>
 * 用于将“用户规则中声明的字段 key”安全地映射为数据库列名，
 * 避免用户配置直接触达 SQL column，防止注入与越权。
 *
 * <p><b>约束：</b>
 * <ul>
 *   <li>column 必须来自白名单，禁止运行时拼接</li>
 *   <li>table 可选（为空时表示使用当前 SQL 的主表）</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 22:26
 */
public record ColumnRef(String table, String column) {
}
