-- MySQL 8.x DDL

CREATE TABLE IF NOT EXISTS tenant_profile (
    tenant_id VARCHAR(64) PRIMARY KEY,
    mode VARCHAR(32) NOT NULL,
    ds_key VARCHAR(128) NULL,
    jdbc_url VARCHAR(512) NULL,
    jdbc_username VARCHAR(128) NULL,
    jdbc_password VARCHAR(256) NULL,
    jdbc_driver VARCHAR(256) NULL,
    ds_type VARCHAR(256) NULL,
    tenant_line_enabled TINYINT(1) NOT NULL DEFAULT 1,
    tenant_version BIGINT NOT NULL DEFAULT 0,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS data_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    tenant_code VARCHAR(64) NOT NULL,
    resource_hashcode VARCHAR(128) NOT NULL,
    resource_code VARCHAR(128) NOT NULL,
    resource_parent_code VARCHAR(128) NULL,
    resource_parent_codes VARCHAR(512) NULL,
    resource_oper_type VARCHAR(1024) NULL,
    resource_name VARCHAR(128) NULL,
    resource_type VARCHAR(64) NOT NULL,
    resource_desc VARCHAR(512) NULL,
    resource_owner VARCHAR(256) NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_resource_hashcode (resource_hashcode),
    UNIQUE KEY uk_tenant_type_code (tenant_code, resource_type, resource_code),
    KEY idx_tenant_resource (tenant_code, resource_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS data_resource_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    tenant_code VARCHAR(64) NOT NULL,
    subject_type VARCHAR(32) NOT NULL,
    subject_id VARCHAR(64) NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    resource_code VARCHAR(128) NOT NULL,
    action VARCHAR(64) NOT NULL,
    policy_effect VARCHAR(16) NOT NULL,
    policy_scope VARCHAR(16) NULL,
    range_dsl TEXT NULL,
    allow_columns TEXT NULL,
    deny_columns TEXT NULL,
    inherit_flag TINYINT(1) NOT NULL DEFAULT 0,
    resource_range VARCHAR(32) NULL,
    never_expire TINYINT(1) NOT NULL DEFAULT 0,
    expire_time DATETIME NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    KEY idx_tenant_subject (tenant_code, subject_type, subject_id),
    KEY idx_tenant_resource (tenant_code, resource_code),
    KEY idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PostgreSQL 13+ DDL

CREATE TABLE IF NOT EXISTS tenant_profile (
    tenant_id VARCHAR(64) PRIMARY KEY,
    mode VARCHAR(32) NOT NULL,
    ds_key VARCHAR(128) NULL,
    jdbc_url VARCHAR(512) NULL,
    jdbc_username VARCHAR(128) NULL,
    jdbc_password VARCHAR(256) NULL,
    jdbc_driver VARCHAR(256) NULL,
    ds_type VARCHAR(256) NULL,
    tenant_line_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_version BIGINT NOT NULL DEFAULT 0,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);

CREATE TABLE IF NOT EXISTS data_resource (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    tenant_code VARCHAR(64) NOT NULL,
    resource_hashcode VARCHAR(128) NOT NULL,
    resource_code VARCHAR(128) NOT NULL,
    resource_parent_code VARCHAR(128) NULL,
    resource_parent_codes VARCHAR(512) NULL,
    resource_oper_type VARCHAR(1024) NULL,
    resource_name VARCHAR(128) NULL,
    resource_type VARCHAR(64) NOT NULL,
    resource_desc VARCHAR(512) NULL,
    resource_owner VARCHAR(256) NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_resource_hashcode ON data_resource(resource_hashcode);
CREATE UNIQUE INDEX IF NOT EXISTS uk_tenant_type_code ON data_resource(tenant_code, resource_type, resource_code);
CREATE INDEX IF NOT EXISTS idx_tenant_resource ON data_resource(tenant_code, resource_code);

CREATE TABLE IF NOT EXISTS data_resource_policy (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    tenant_code VARCHAR(64) NOT NULL,
    subject_type VARCHAR(32) NOT NULL,
    subject_id VARCHAR(64) NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    resource_code VARCHAR(128) NOT NULL,
    action VARCHAR(64) NOT NULL,
    policy_effect VARCHAR(16) NOT NULL,
    policy_scope VARCHAR(16) NULL,
    range_dsl TEXT NULL,
    allow_columns TEXT NULL,
    deny_columns TEXT NULL,
    inherit_flag BOOLEAN NOT NULL DEFAULT FALSE,
    resource_range VARCHAR(32) NULL,
    never_expire BOOLEAN NOT NULL DEFAULT FALSE,
    expire_time TIMESTAMP NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);

CREATE INDEX IF NOT EXISTS idx_tenant_subject ON data_resource_policy(tenant_code, subject_type, subject_id);
CREATE INDEX IF NOT EXISTS idx_tenant_resource_policy ON data_resource_policy(tenant_code, resource_code);
CREATE INDEX IF NOT EXISTS idx_action ON data_resource_policy(action);

-- SQLite 3.x DDL

CREATE TABLE IF NOT EXISTS tenant_profile (
    tenant_id TEXT PRIMARY KEY,
    mode TEXT NOT NULL,
    ds_key TEXT NULL,
    jdbc_url TEXT NULL,
    jdbc_username TEXT NULL,
    jdbc_password TEXT NULL,
    jdbc_driver TEXT NULL,
    ds_type TEXT NULL,
    tenant_line_enabled INTEGER NOT NULL DEFAULT 1,
    tenant_version INTEGER NOT NULL DEFAULT 0,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);

CREATE TABLE IF NOT EXISTS data_resource (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    tenant_code TEXT NOT NULL,
    resource_hashcode TEXT NOT NULL,
    resource_code TEXT NOT NULL,
    resource_parent_code TEXT NULL,
    resource_parent_codes TEXT NULL,
    resource_oper_type TEXT NULL,
    resource_name TEXT NULL,
    resource_type TEXT NOT NULL,
    resource_desc TEXT NULL,
    resource_owner TEXT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_resource_hashcode ON data_resource(resource_hashcode);
CREATE UNIQUE INDEX IF NOT EXISTS uk_tenant_type_code ON data_resource(tenant_code, resource_type, resource_code);
CREATE INDEX IF NOT EXISTS idx_tenant_resource ON data_resource(tenant_code, resource_code);

CREATE TABLE IF NOT EXISTS data_resource_policy (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    tenant_code TEXT NOT NULL,
    subject_type TEXT NOT NULL,
    subject_id TEXT NOT NULL,
    resource_type TEXT NOT NULL,
    resource_code TEXT NOT NULL,
    action TEXT NOT NULL,
    policy_effect TEXT NOT NULL,
    policy_scope TEXT NULL,
    range_dsl TEXT NULL,
    allow_columns TEXT NULL,
    deny_columns TEXT NULL,
    inherit_flag INTEGER NOT NULL DEFAULT 0,
    resource_range TEXT NULL,
    never_expire INTEGER NOT NULL DEFAULT 0,
    expire_time DATETIME NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);

CREATE INDEX IF NOT EXISTS idx_tenant_subject ON data_resource_policy(tenant_code, subject_type, subject_id);
CREATE INDEX IF NOT EXISTS idx_tenant_resource_policy ON data_resource_policy(tenant_code, resource_code);
CREATE INDEX IF NOT EXISTS idx_action ON data_resource_policy(action);
