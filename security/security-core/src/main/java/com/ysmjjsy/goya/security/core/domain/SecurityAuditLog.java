package com.ysmjjsy.goya.security.core.domain;

import com.ysmjjsy.goya.component.web.utils.UserAgent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p>安全审计日志实体</p>
 * <p>记录关键安全操作，支持安全审计</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "安全审计日志")
public class SecurityAuditLog {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "操作类型（LOGIN_SUCCESS, LOGIN_FAILURE, TOKEN_GENERATE, TOKEN_REFRESH, TOKEN_REVOKE, AUTHORIZATION_CODE_GENERATE, CLIENT_AUTH_FAILURE等）")
    private String operation;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "User-Agent")
    private UserAgent userAgent;

    @Schema(description = "请求URI")
    private String requestUri;

    @Schema(description = "请求方法")
    private String requestMethod;

    @Schema(description = "操作状态（SUCCESS, FAILURE）")
    private String status;

    @Schema(description = "错误消息（如果失败）")
    private String errorMessage;

    @Schema(description = "客户端ID（OAuth2相关操作）")
    private String clientId;

    @Schema(description = "时间戳")
    private LocalDateTime timestamp;
}

