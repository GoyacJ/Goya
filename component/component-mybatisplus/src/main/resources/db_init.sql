-- MySQL 8.x DDL

CREATE TABLE IF NOT EXISTS tenant_profile (
    tenant_id VARCHAR(64) PRIMARY KEY,
    mode VARCHAR(32) NOT NULL,
    ds_key VARCHAR(128) NOT NULL,
    tenant_line_enabled TINYINT(1) NOT NULL DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS data_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_code VARCHAR(64) NOT NULL,
    resource_hashcode VARCHAR(128) NOT NULL,
    resource_code VARCHAR(128) NOT NULL,
    resource_parent_code VARCHAR(128) NULL,
    resource_parent_codes VARCHAR(512) NULL,
    resource_oper_type VARCHAR(1024) NULL,
    resource_name VARCHAR(128) NULL,
    resource_type VARCHAR(64) NOT NULL,
    resource_type_name VARCHAR(128) NULL,
    resource_desc VARCHAR(512) NULL,
    resource_owner VARCHAR(256) NULL,
    create_time DATETIME NOT NULL,
    UNIQUE KEY uk_resource_hashcode (resource_hashcode),
    KEY idx_tenant_resource (tenant_code, resource_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS data_resource_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_code VARCHAR(64) NOT NULL,
    subject_type VARCHAR(32) NOT NULL,
    subject_id VARCHAR(64) NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    resource_code VARCHAR(128) NOT NULL,
    action_code VARCHAR(64) NOT NULL,
    action_name VARCHAR(128) NULL,
    policy_effect VARCHAR(16) NOT NULL,
    policy_scope VARCHAR(16) NULL,
    range_dsl TEXT NULL,
    allow_columns TEXT NULL,
    deny_columns TEXT NULL,
    inherit_flag TINYINT(1) NOT NULL DEFAULT 0,
    resource_range VARCHAR(32) NULL,
    never_expire TINYINT(1) NOT NULL DEFAULT 0,
    expire_time DATETIME NULL,
    KEY idx_tenant_subject (tenant_code, subject_type, subject_id),
    KEY idx_tenant_resource (tenant_code, resource_code),
    KEY idx_action (action_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
