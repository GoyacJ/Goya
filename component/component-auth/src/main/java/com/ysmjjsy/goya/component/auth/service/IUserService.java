package com.ysmjjsy.goya.component.auth.service;

import com.ysmjjsy.goya.component.auth.domain.UserAuthAuditLog;
import com.ysmjjsy.goya.component.auth.domain.UserDevice;
import com.ysmjjsy.goya.component.auth.domain.UserPrincipal;
import com.ysmjjsy.goya.component.social.enums.SocialTypeEnum;
import com.ysmjjsy.goya.component.social.service.dto.SocialDetails;
import com.ysmjjsy.goya.component.web.utils.UserAgent;

import java.util.List;

/**
 * <p>用户服务</p>
 *
 * @author goya
 * @since 2026/1/5 22:58
 */
public interface IUserService {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    UserPrincipal findUserByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phoneNumber 手机号
     * @return 用户
     */
    UserPrincipal findUserByPhone(String phoneNumber);

    /**
     * 根据 openId 查询用户
     *
     * @param openId     openId
     * @param socialType 类型
     * @return 用户
     */
    UserPrincipal findUserByOpenId(String openId, SocialTypeEnum socialType);

    /**
     * 注册用户
     *
     * @param userPrincipal 用户信息
     * @return 用户信息
     */
    UserPrincipal registerUser(UserPrincipal userPrincipal);

    /**
     * 注册用户
     *
     * @param socialDetails 用户信息
     * @return 用户信息
     */
    UserPrincipal registerUser(SocialDetails socialDetails);

    /**
     * 注册设备
     *
     * @param userDevice 用户设备信息
     * @return 设备信息
     */
    UserDevice registerDevice(UserDevice userDevice);

    /**
     * 根据用户ID查询设备列表
     *
     * @param userId 用户ID
     * @return 设备列表
     */
    List<UserDevice> findByUserId(String userId);

    /**
     * 根据设备ID查询设备
     *
     * @param deviceId 设备ID
     * @return 设备信息
     */
    UserDevice findByDeviceId(String deviceId);

    /**
     * 信任设备
     *
     * @param deviceId 设备ID
     */
    void trustDevice(String deviceId);

    /**
     * 撤销设备信任
     *
     * @param deviceId 设备ID
     */
    void revokeDevice(String deviceId);

    /**
     * 更新设备最后登录时间
     *
     * @param deviceId 设备ID
     */
    void updateLastLoginTime(String deviceId);

    /**
     * 删除设备
     *
     * @param deviceId 设备ID
     */
    void deleteDevice(String deviceId);

    /**
     * 记录审计日志
     * <p>异步执行，不阻塞主流程</p>
     *
     * @param auditLog 审计日志
     */
    void recordAuditLog(UserAuthAuditLog auditLog);

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
        UserAuthAuditLog auditLog = UserAuthAuditLog.builder()
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
        UserAuthAuditLog auditLog = UserAuthAuditLog.builder()
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
        UserAuthAuditLog auditLog = UserAuthAuditLog.builder()
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
        UserAuthAuditLog auditLog = UserAuthAuditLog.builder()
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
        UserAuthAuditLog auditLog = UserAuthAuditLog.builder()
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
        UserAuthAuditLog auditLog = UserAuthAuditLog.builder()
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
        UserAuthAuditLog auditLog = UserAuthAuditLog.builder()
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
