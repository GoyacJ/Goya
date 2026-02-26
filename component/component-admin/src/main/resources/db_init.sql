-- MySQL 8.x DDL

CREATE TABLE IF NOT EXISTS iam_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    username VARCHAR(128) NOT NULL,
    password VARCHAR(255) NULL,
    nickname VARCHAR(128) NULL,
    phone_number VARCHAR(64) NULL,
    email VARCHAR(128) NULL,
    avatar VARCHAR(512) NULL,
    open_id VARCHAR(128) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    password_changed_at DATETIME NULL,
    last_login_at DATETIME NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_user_tenant_username (tenant_id, username),
    UNIQUE KEY uk_iam_user_tenant_phone (tenant_id, phone_number),
    UNIQUE KEY uk_iam_user_tenant_email (tenant_id, email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_user_password_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    password VARCHAR(255) NOT NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    KEY idx_iam_user_password_history_user (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_user_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(128) NOT NULL,
    device_name VARCHAR(128) NULL,
    device_type VARCHAR(32) NULL,
    trusted TINYINT(1) NOT NULL DEFAULT 0,
    ip_address VARCHAR(64) NULL,
    user_agent TEXT NULL,
    last_login_time DATETIME NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_user_device (tenant_id, user_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_user_auth_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NULL,
    username VARCHAR(128) NULL,
    operation VARCHAR(64) NULL,
    ip_address VARCHAR(64) NULL,
    user_agent TEXT NULL,
    request_uri VARCHAR(512) NULL,
    request_method VARCHAR(32) NULL,
    status VARCHAR(32) NULL,
    error_message VARCHAR(512) NULL,
    client_id VARCHAR(128) NULL,
    timestamp DATETIME NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    KEY idx_iam_user_auth_audit_log_user (tenant_id, user_id),
    KEY idx_iam_user_auth_audit_log_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    role_code VARCHAR(128) NOT NULL,
    role_name VARCHAR(128) NOT NULL,
    data_scope VARCHAR(64) NULL,
    status VARCHAR(32) NULL,
    remark VARCHAR(512) NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_role_tenant_role_code (tenant_id, role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(128) NOT NULL,
    resource_type VARCHAR(64) NULL,
    resource_code VARCHAR(256) NULL,
    action_code VARCHAR(64) NULL,
    status VARCHAR(32) NULL,
    remark VARCHAR(512) NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_permission_tenant_code (tenant_id, permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_user_role (tenant_id, user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_role_permission (tenant_id, role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    menu_code VARCHAR(128) NOT NULL,
    menu_name VARCHAR(128) NOT NULL,
    menu_type VARCHAR(32) NOT NULL,
    path VARCHAR(256) NULL,
    component VARCHAR(256) NULL,
    permission_code VARCHAR(128) NULL,
    icon VARCHAR(128) NULL,
    parent_id BIGINT NULL,
    parent_ids VARCHAR(1024) NULL,
    sort INT NULL,
    status VARCHAR(32) NULL,
    hidden TINYINT(1) NOT NULL DEFAULT 0,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_menu_tenant_code (tenant_id, menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_role_menu (tenant_id, role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    dept_code VARCHAR(128) NOT NULL,
    dept_name VARCHAR(128) NOT NULL,
    parent_id BIGINT NULL,
    parent_ids VARCHAR(1024) NULL,
    leader VARCHAR(128) NULL,
    phone VARCHAR(64) NULL,
    email VARCHAR(128) NULL,
    sort INT NULL,
    status VARCHAR(32) NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_dept_tenant_code (tenant_id, dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_user_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_user_dept (tenant_id, user_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_role_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_role_dept (tenant_id, role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_dict_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    dict_type_code VARCHAR(128) NOT NULL,
    dict_type_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NULL,
    remark VARCHAR(512) NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_dict_type_tenant_code (tenant_id, dict_type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iam_dict_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    dict_type_code VARCHAR(128) NOT NULL,
    item_code VARCHAR(128) NOT NULL,
    item_label VARCHAR(128) NOT NULL,
    item_value VARCHAR(512) NULL,
    sort INT NULL,
    status VARCHAR(32) NULL,
    remark VARCHAR(512) NULL,
    del_flag TINYINT(1) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by VARCHAR(64) NULL,
    updated_at DATETIME NULL,
    updated_by VARCHAR(64) NULL,
    UNIQUE KEY uk_iam_dict_item_tenant_type_item (tenant_id, dict_type_code, item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PostgreSQL 13+ DDL

CREATE TABLE IF NOT EXISTS iam_user (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    username VARCHAR(128) NOT NULL,
    password VARCHAR(255) NULL,
    nickname VARCHAR(128) NULL,
    phone_number VARCHAR(64) NULL,
    email VARCHAR(128) NULL,
    avatar VARCHAR(512) NULL,
    open_id VARCHAR(128) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    password_changed_at TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_tenant_username ON iam_user(tenant_id, username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_tenant_phone ON iam_user(tenant_id, phone_number);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_tenant_email ON iam_user(tenant_id, email);

CREATE TABLE IF NOT EXISTS iam_user_password_history (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    password VARCHAR(255) NOT NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE INDEX IF NOT EXISTS idx_iam_user_password_history_user ON iam_user_password_history(tenant_id, user_id);

CREATE TABLE IF NOT EXISTS iam_user_device (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(128) NOT NULL,
    device_name VARCHAR(128) NULL,
    device_type VARCHAR(32) NULL,
    trusted BOOLEAN NOT NULL DEFAULT FALSE,
    ip_address VARCHAR(64) NULL,
    user_agent TEXT NULL,
    last_login_time TIMESTAMP NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_device ON iam_user_device(tenant_id, user_id, device_id);

CREATE TABLE IF NOT EXISTS iam_user_auth_audit_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NULL,
    username VARCHAR(128) NULL,
    operation VARCHAR(64) NULL,
    ip_address VARCHAR(64) NULL,
    user_agent TEXT NULL,
    request_uri VARCHAR(512) NULL,
    request_method VARCHAR(32) NULL,
    status VARCHAR(32) NULL,
    error_message VARCHAR(512) NULL,
    client_id VARCHAR(128) NULL,
    timestamp TIMESTAMP NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE INDEX IF NOT EXISTS idx_iam_user_auth_audit_log_user ON iam_user_auth_audit_log(tenant_id, user_id);
CREATE INDEX IF NOT EXISTS idx_iam_user_auth_audit_log_timestamp ON iam_user_auth_audit_log(timestamp);

CREATE TABLE IF NOT EXISTS iam_role (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    role_code VARCHAR(128) NOT NULL,
    role_name VARCHAR(128) NOT NULL,
    data_scope VARCHAR(64) NULL,
    status VARCHAR(32) NULL,
    remark VARCHAR(512) NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_role_tenant_role_code ON iam_role(tenant_id, role_code);

CREATE TABLE IF NOT EXISTS iam_permission (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(128) NOT NULL,
    resource_type VARCHAR(64) NULL,
    resource_code VARCHAR(256) NULL,
    action_code VARCHAR(64) NULL,
    status VARCHAR(32) NULL,
    remark VARCHAR(512) NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_permission_tenant_code ON iam_permission(tenant_id, permission_code);

CREATE TABLE IF NOT EXISTS iam_user_role (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_role ON iam_user_role(tenant_id, user_id, role_id);

CREATE TABLE IF NOT EXISTS iam_role_permission (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_role_permission ON iam_role_permission(tenant_id, role_id, permission_id);

CREATE TABLE IF NOT EXISTS iam_menu (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    menu_code VARCHAR(128) NOT NULL,
    menu_name VARCHAR(128) NOT NULL,
    menu_type VARCHAR(32) NOT NULL,
    path VARCHAR(256) NULL,
    component VARCHAR(256) NULL,
    permission_code VARCHAR(128) NULL,
    icon VARCHAR(128) NULL,
    parent_id BIGINT NULL,
    parent_ids VARCHAR(1024) NULL,
    sort INT NULL,
    status VARCHAR(32) NULL,
    hidden BOOLEAN NOT NULL DEFAULT FALSE,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_menu_tenant_code ON iam_menu(tenant_id, menu_code);

CREATE TABLE IF NOT EXISTS iam_role_menu (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_role_menu ON iam_role_menu(tenant_id, role_id, menu_id);

CREATE TABLE IF NOT EXISTS iam_dept (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    dept_code VARCHAR(128) NOT NULL,
    dept_name VARCHAR(128) NOT NULL,
    parent_id BIGINT NULL,
    parent_ids VARCHAR(1024) NULL,
    leader VARCHAR(128) NULL,
    phone VARCHAR(64) NULL,
    email VARCHAR(128) NULL,
    sort INT NULL,
    status VARCHAR(32) NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_dept_tenant_code ON iam_dept(tenant_id, dept_code);

CREATE TABLE IF NOT EXISTS iam_user_dept (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_dept ON iam_user_dept(tenant_id, user_id, dept_id);

CREATE TABLE IF NOT EXISTS iam_role_dept (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_role_dept ON iam_role_dept(tenant_id, role_id, dept_id);

CREATE TABLE IF NOT EXISTS iam_dict_type (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    dict_type_code VARCHAR(128) NOT NULL,
    dict_type_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NULL,
    remark VARCHAR(512) NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_dict_type_tenant_code ON iam_dict_type(tenant_id, dict_type_code);

CREATE TABLE IF NOT EXISTS iam_dict_item (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    dict_type_code VARCHAR(128) NOT NULL,
    item_code VARCHAR(128) NOT NULL,
    item_label VARCHAR(128) NOT NULL,
    item_value VARCHAR(512) NULL,
    sort INT NULL,
    status VARCHAR(32) NULL,
    remark VARCHAR(512) NULL,
    del_flag BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    created_by VARCHAR(64) NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(64) NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_dict_item_tenant_type_item ON iam_dict_item(tenant_id, dict_type_code, item_code);

-- SQLite 3.x DDL

CREATE TABLE IF NOT EXISTS iam_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    username TEXT NOT NULL,
    password TEXT NULL,
    nickname TEXT NULL,
    phone_number TEXT NULL,
    email TEXT NULL,
    avatar TEXT NULL,
    open_id TEXT NULL,
    status TEXT NOT NULL DEFAULT 'ENABLED',
    password_changed_at DATETIME NULL,
    last_login_at DATETIME NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_tenant_username ON iam_user(tenant_id, username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_tenant_phone ON iam_user(tenant_id, phone_number);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_tenant_email ON iam_user(tenant_id, email);

CREATE TABLE IF NOT EXISTS iam_user_password_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    user_id INTEGER NOT NULL,
    password TEXT NOT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE INDEX IF NOT EXISTS idx_iam_user_password_history_user ON iam_user_password_history(tenant_id, user_id);

CREATE TABLE IF NOT EXISTS iam_user_device (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    user_id INTEGER NOT NULL,
    device_id TEXT NOT NULL,
    device_name TEXT NULL,
    device_type TEXT NULL,
    trusted INTEGER NOT NULL DEFAULT 0,
    ip_address TEXT NULL,
    user_agent TEXT NULL,
    last_login_time DATETIME NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_device ON iam_user_device(tenant_id, user_id, device_id);

CREATE TABLE IF NOT EXISTS iam_user_auth_audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    user_id INTEGER NULL,
    username TEXT NULL,
    operation TEXT NULL,
    ip_address TEXT NULL,
    user_agent TEXT NULL,
    request_uri TEXT NULL,
    request_method TEXT NULL,
    status TEXT NULL,
    error_message TEXT NULL,
    client_id TEXT NULL,
    timestamp DATETIME NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE INDEX IF NOT EXISTS idx_iam_user_auth_audit_log_user ON iam_user_auth_audit_log(tenant_id, user_id);
CREATE INDEX IF NOT EXISTS idx_iam_user_auth_audit_log_timestamp ON iam_user_auth_audit_log(timestamp);

CREATE TABLE IF NOT EXISTS iam_role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    role_code TEXT NOT NULL,
    role_name TEXT NOT NULL,
    data_scope TEXT NULL,
    status TEXT NULL,
    remark TEXT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_role_tenant_role_code ON iam_role(tenant_id, role_code);

CREATE TABLE IF NOT EXISTS iam_permission (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    permission_code TEXT NOT NULL,
    permission_name TEXT NOT NULL,
    resource_type TEXT NULL,
    resource_code TEXT NULL,
    action_code TEXT NULL,
    status TEXT NULL,
    remark TEXT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_permission_tenant_code ON iam_permission(tenant_id, permission_code);

CREATE TABLE IF NOT EXISTS iam_user_role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_role ON iam_user_role(tenant_id, user_id, role_id);

CREATE TABLE IF NOT EXISTS iam_role_permission (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_role_permission ON iam_role_permission(tenant_id, role_id, permission_id);

CREATE TABLE IF NOT EXISTS iam_menu (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    menu_code TEXT NOT NULL,
    menu_name TEXT NOT NULL,
    menu_type TEXT NOT NULL,
    path TEXT NULL,
    component TEXT NULL,
    permission_code TEXT NULL,
    icon TEXT NULL,
    parent_id INTEGER NULL,
    parent_ids TEXT NULL,
    sort INTEGER NULL,
    status TEXT NULL,
    hidden INTEGER NOT NULL DEFAULT 0,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_menu_tenant_code ON iam_menu(tenant_id, menu_code);

CREATE TABLE IF NOT EXISTS iam_role_menu (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    role_id INTEGER NOT NULL,
    menu_id INTEGER NOT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_role_menu ON iam_role_menu(tenant_id, role_id, menu_id);

CREATE TABLE IF NOT EXISTS iam_dept (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    dept_code TEXT NOT NULL,
    dept_name TEXT NOT NULL,
    parent_id INTEGER NULL,
    parent_ids TEXT NULL,
    leader TEXT NULL,
    phone TEXT NULL,
    email TEXT NULL,
    sort INTEGER NULL,
    status TEXT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_dept_tenant_code ON iam_dept(tenant_id, dept_code);

CREATE TABLE IF NOT EXISTS iam_user_dept (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    user_id INTEGER NOT NULL,
    dept_id INTEGER NOT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_user_dept ON iam_user_dept(tenant_id, user_id, dept_id);

CREATE TABLE IF NOT EXISTS iam_role_dept (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    role_id INTEGER NOT NULL,
    dept_id INTEGER NOT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_role_dept ON iam_role_dept(tenant_id, role_id, dept_id);

CREATE TABLE IF NOT EXISTS iam_dict_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    dict_type_code TEXT NOT NULL,
    dict_type_name TEXT NOT NULL,
    status TEXT NULL,
    remark TEXT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_dict_type_tenant_code ON iam_dict_type(tenant_id, dict_type_code);

CREATE TABLE IF NOT EXISTS iam_dict_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id TEXT NOT NULL,
    dict_type_code TEXT NOT NULL,
    item_code TEXT NOT NULL,
    item_label TEXT NOT NULL,
    item_value TEXT NULL,
    sort INTEGER NULL,
    status TEXT NULL,
    remark TEXT NULL,
    del_flag INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    created_by TEXT NULL,
    updated_at DATETIME NULL,
    updated_by TEXT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_iam_dict_item_tenant_type_item ON iam_dict_item(tenant_id, dict_type_code, item_code);
