package com.ysmjjsy.goya.security.core.service;

import com.ysmjjsy.goya.component.web.utils.UserAgent;
import com.ysmjjsy.goya.security.core.domain.SecurityAuditLog;

/**
 * <p>安全审计服务</p>
 * <p>提供异步记录审计日志的方法</p>
 * <p>注意：此服务需要实现类来持久化审计日志（数据库、文件等）</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface SecurityAuditService {

    /**
     * 记录审计日志
     * <p>异步执行，不阻塞主流程</p>
     *
     * @param auditLog 审计日志
     */
    void recordAuditLog(SecurityAuditLog auditLog);

    /**
     * 记录登录成功审计日志
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param tenantId   租户ID
     * @param ipAddress  IP地址
     * @param userAgent  User-Agent
     * @param requestUri 请求URI
     */
    default void recordLoginSuccess(String userId, String username, String tenantId,
                                    String ipAddress, UserAgent userAgent, String requestUri) {
        SecurityAuditLog auditLog = SecurityAuditLog.builder()
                .userId(userId)
                .username(username)
                .tenantId(tenantId)
                .operation("LOGIN_SUCCESS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .requestUri(requestUri)
                .requestMethod("POST")
                .status("SUCCESS")
                .build();
        recordAuditLog(auditLog);
    }

    /**
     * 记录登录失败审计日志
     *
     * @param username     用户名
     * @param ipAddress    IP地址
     * @param userAgent    User-Agent
     * @param requestUri   请求URI
     * @param errorMessage 错误消息
     */
    default void recordLoginFailure(String username, String ipAddress, UserAgent userAgent,
                                    String requestUri, String errorMessage) {
        SecurityAuditLog auditLog = SecurityAuditLog.builder()
                .username(username)
                .operation("LOGIN_FAILURE")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .requestUri(requestUri)
                .requestMethod("POST")
                .status("FAILURE")
                .errorMessage(errorMessage)
                .build();
        recordAuditLog(auditLog);
    }

    /**
     * 记录Token生成审计日志
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param clientId   客户端ID
     * @param ipAddress  IP地址
     * @param requestUri 请求URI
     */
    default void recordTokenGenerate(String userId, String username, String clientId,
                                     String ipAddress, String requestUri) {
        SecurityAuditLog auditLog = SecurityAuditLog.builder()
                .userId(userId)
                .username(username)
                .clientId(clientId)
                .operation("TOKEN_GENERATE")
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .requestMethod("POST")
                .status("SUCCESS")
                .build();
        recordAuditLog(auditLog);
    }

    /**
     * 记录Token刷新审计日志
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param clientId   客户端ID
     * @param ipAddress  IP地址
     * @param requestUri 请求URI
     */
    default void recordTokenRefresh(String userId, String username, String clientId,
                                    String ipAddress, String requestUri) {
        SecurityAuditLog auditLog = SecurityAuditLog.builder()
                .userId(userId)
                .username(username)
                .clientId(clientId)
                .operation("TOKEN_REFRESH")
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .requestMethod("POST")
                .status("SUCCESS")
                .build();
        recordAuditLog(auditLog);
    }

    /**
     * 记录Token撤销审计日志
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param clientId   客户端ID
     * @param ipAddress  IP地址
     * @param requestUri 请求URI
     */
    default void recordTokenRevoke(String userId, String username, String clientId,
                                   String ipAddress, String requestUri) {
        SecurityAuditLog auditLog = SecurityAuditLog.builder()
                .userId(userId)
                .username(username)
                .clientId(clientId)
                .operation("TOKEN_REVOKE")
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .requestMethod("POST")
                .status("SUCCESS")
                .build();
        recordAuditLog(auditLog);
    }

    /**
     * 记录授权码生成审计日志
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param clientId   客户端ID
     * @param ipAddress  IP地址
     * @param requestUri 请求URI
     */
    default void recordAuthorizationCodeGenerate(String userId, String username, String clientId,
                                                 String ipAddress, String requestUri) {
        SecurityAuditLog auditLog = SecurityAuditLog.builder()
                .userId(userId)
                .username(username)
                .clientId(clientId)
                .operation("AUTHORIZATION_CODE_GENERATE")
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .requestMethod("GET")
                .status("SUCCESS")
                .build();
        recordAuditLog(auditLog);
    }

    /**
     * 记录客户端认证失败审计日志
     *
     * @param clientId     客户端ID
     * @param ipAddress    IP地址
     * @param requestUri   请求URI
     * @param errorMessage 错误消息
     */
    default void recordClientAuthFailure(String clientId, String ipAddress,
                                         String requestUri, String errorMessage) {
        SecurityAuditLog auditLog = SecurityAuditLog.builder()
                .clientId(clientId)
                .operation("CLIENT_AUTH_FAILURE")
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .requestMethod("POST")
                .status("FAILURE")
                .errorMessage(errorMessage)
                .build();
        recordAuditLog(auditLog);
    }
}

