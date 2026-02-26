package com.ysmjjsy.goya.component.admin.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>认证审计日志实体</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_user_auth_audit_log")
public class IamUserAuthAuditLogEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 3122276481187859482L;

    @TableField("user_id")
    private String userId;

    @TableField("username")
    private String username;

    @TableField("operation")
    private String operation;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("request_uri")
    private String requestUri;

    @TableField("request_method")
    private String requestMethod;

    @TableField("status")
    private String status;

    @TableField("error_message")
    private String errorMessage;

    @TableField("client_id")
    private String clientId;

    @TableField("timestamp")
    private LocalDateTime timestamp;
}
