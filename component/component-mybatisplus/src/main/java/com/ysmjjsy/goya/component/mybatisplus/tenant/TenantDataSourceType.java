package com.ysmjjsy.goya.component.mybatisplus.tenant;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>租户数据源类型</p>
 *
 * @author goya
 * @since 2026/1/31 13:10
 */
@Getter
@AllArgsConstructor
public enum TenantDataSourceType implements CodeEnum<String> {

    MYSQL("MYSQL", "MySQL", "com.mysql.cj.jdbc.Driver"),
    POSTGRESQL("POSTGRESQL", "PostgreSQL", "org.postgresql.Driver"),
    SQLITE("SQLITE", "SQLite", "org.sqlite.JDBC");

    private final String code;
    private final String label;
    private final String driverClassName;
}
