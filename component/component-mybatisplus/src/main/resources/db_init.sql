CREATE TABLE tenant_profile
(
    tenant_id           VARCHAR(64) NOT NULL COMMENT '租户ID',
    mode                VARCHAR(32) NOT NULL COMMENT '租户模式：CORE_SHARED / DEDICATED_DB',
    ds_key              VARCHAR(64) NOT NULL COMMENT 'dynamic-datasource 的 dsKey',
    tenant_line_enabled TINYINT(1)  NOT NULL DEFAULT 1 COMMENT '是否启用 tenant line（独库可关闭）',
    version             BIGINT      NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    updated_at          DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by          VARCHAR(64) NULL COMMENT '更新人',
    created_at          DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by          VARCHAR(64) NULL COMMENT '创建人',
    PRIMARY KEY (tenant_id)
) COMMENT ='租户画像配置（框架内置）';


CREATE TABLE permission_rule_set (
                                     id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                     tenant_id     VARCHAR(64)  NOT NULL COMMENT '租户ID',
                                     subject_id    VARCHAR(128) NOT NULL COMMENT '主体ID（userId/roleId/组合主体）',
                                     resource      VARCHAR(64)  NOT NULL COMMENT '逻辑资源（如 ORDER/CUSTOMER）',
                                     rule_json     JSON         NOT NULL COMMENT '规则集JSON（结构化，禁止 raw SQL）',
                                     enabled       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用',
                                     updated_at    DATETIME(3)  NOT NULL COMMENT '更新时间',
                                     updated_by    VARCHAR(64)  NULL COMMENT '更新人',
                                     created_at    DATETIME(3)  NOT NULL COMMENT '创建时间',
                                     created_by    VARCHAR(64)  NULL COMMENT '创建人',
                                     UNIQUE KEY uk_tenant_subject_resource (tenant_id, subject_id, resource),
                                     PRIMARY KEY (id)
) COMMENT='权限规则集（subject+resource）';


CREATE TABLE permission_subject_version (
                                            tenant_id   VARCHAR(64)  NOT NULL COMMENT '租户ID',
                                            subject_id  VARCHAR(128) NOT NULL COMMENT '主体ID',
                                            version     BIGINT       NOT NULL DEFAULT 0 COMMENT '主体版本号（任一资源规则变化即递增）',
                                            updated_at  DATETIME(3)  NOT NULL COMMENT '更新时间',
                                            PRIMARY KEY (tenant_id, subject_id)
) COMMENT='权限主体版本（用于快速生效）';