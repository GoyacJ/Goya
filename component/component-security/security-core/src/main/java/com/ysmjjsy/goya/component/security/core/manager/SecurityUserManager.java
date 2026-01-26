package com.ysmjjsy.goya.component.security.core.manager;

import com.ysmjjsy.goya.component.framework.enums.StatusEnum;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUserAuthAuditLog;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUserDevice;
import com.ysmjjsy.goya.component.security.core.enums.SecurityOperationEnum;
import com.ysmjjsy.goya.component.security.core.service.ISocialUserService;
import com.ysmjjsy.goya.component.security.core.service.IUserService;
import com.ysmjjsy.goya.component.web.enums.RequestMethodEnum;
import com.ysmjjsy.goya.component.web.utils.UserAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;

/**
 * <p>用户服务接口</p>
 *
 * @author goya
 * @since 2025/10/10 14:25
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityUserManager implements UserDetailsService {

    private final IUserService userService;
    private final ObjectProvider<ISocialUserService> socialUserServiceProvider;

    /**
     * 用户锁定
     *
     * @param userId 用户Id
     */
    public void lockedUser(String userId) {
        userService.lockedUser(userId);
    }

    /**
     * 根据手机号获取用户并保存
     *
     * @param phoneNumber 手机号
     * @return 用户
     */
    public SecurityUser smsLoginAndSave(String phoneNumber) {
        SecurityUser user = userService.findUserByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + phoneNumber);
        }
        return user;
    }

    /**
     *
     * 根据source+principal获取用户并保存
     *
     * @param source    source
     * @param attributes attributes
     * @return SocialUser
     */
    public SecurityUser thirdLoginAndSave(String source, java.util.Map<String, Object> attributes) {
        ISocialUserService socialUserService = socialUserServiceProvider.getIfAvailable();
        if (socialUserService == null) {
            throw new UsernameNotFoundException("社交登录服务未配置");
        }
        String userId = socialUserService.resolveUserId(source, attributes);
        if (userId == null) {
            throw new UsernameNotFoundException("社交用户未绑定: " + source);
        }
        return findUserByUserId(userId);
    }

    /**
     * 根据openId+unionId获取用户并保存
     *
     * @param openId        openId
     * @param appId         appId
     * @param sessionKey    sessionKey
     * @param encryptedData encryptedData
     * @param iv            iv
     * @return SocialUser
     */
    public SecurityUser wxAppLoginAndSave(String openId,
                                          String appId,
                                          String sessionKey,
                                          String encryptedData,
                                          String iv) {
        ISocialUserService socialUserService = socialUserServiceProvider.getIfAvailable();
        if (socialUserService == null) {
            throw new UsernameNotFoundException("社交登录服务未配置");
        }
        String userId = socialUserService.resolveUserIdForWxApp(openId, appId, sessionKey, encryptedData, iv);
        if (userId == null) {
            throw new UsernameNotFoundException("社交用户未绑定: " + openId);
        }
        return findUserByUserId(userId);
    }

    /**
     * 根据设备ID查询设备
     *
     * @param deviceId 设备ID
     * @return 设备信息
     */
    public SecurityUserDevice findByDeviceId(String deviceId) {
        return userService.findByDeviceId(deviceId);
    }

    /**
     * 根据用户名查询用户
     *
     * @param userId userId
     * @return 用户
     * @throws AuthenticationException 异常
     */
    public SecurityUser findUserByUserId(String userId) throws AuthenticationException {
        return userService.findUserByUserId(userId);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     * @throws AuthenticationException 异常
     */
    public SecurityUser findUserByUsername(String username) throws AuthenticationException {
        return userService.findUserByUsername(username);
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findUserByUsername(username);
    }

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
    public void recordLoginSuccess(String userId, String username, String tenantId,
                                   String ipAddress, UserAgent userAgent, String requestUri) {
        SecurityUserAuthAuditLog auditLog = SecurityUserAuthAuditLog.builder()
                .userId(userId)
                .username(username)
                .tenantId(tenantId)
                .operation(SecurityOperationEnum.LOGIN_SUCCESS)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .requestUri(requestUri)
                .requestMethod(RequestMethodEnum.POST)
                .status(StatusEnum.SUCCESS)
                .build();
        userService.recordAuditLog(auditLog);
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
    public void recordLoginFailure(String username, String ipAddress, UserAgent userAgent,
                                   String requestUri, String errorMessage) {
        SecurityUserAuthAuditLog auditLog = SecurityUserAuthAuditLog.builder()
                .username(username)
                .operation(SecurityOperationEnum.LOGIN_FAILURE)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .requestUri(requestUri)
                .requestMethod(RequestMethodEnum.POST)
                .status(StatusEnum.FAILURE)
                .errorMessage(errorMessage)
                .build();
        userService.recordAuditLog(auditLog);
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
    public void recordTokenGenerate(String userId, String username, String clientId,
                                    String ipAddress, String requestUri) {
        SecurityUserAuthAuditLog auditLog = SecurityUserAuthAuditLog.builder()
                .userId(userId)
                .username(username)
                .clientId(clientId)
                .operation(SecurityOperationEnum.TOKEN_GENERATE)
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .requestMethod(RequestMethodEnum.POST)
                .status(StatusEnum.SUCCESS)
                .build();
        userService.recordAuditLog(auditLog);
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
    public void recordTokenRefresh(String userId, String username, String clientId,
                                   String ipAddress, String requestUri) {
        SecurityUserAuthAuditLog auditLog = SecurityUserAuthAuditLog.builder()
                .userId(userId)
                .username(username)
                .clientId(clientId)
                .operation(SecurityOperationEnum.TOKEN_REFRESH)
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .requestMethod(RequestMethodEnum.POST)
                .status(StatusEnum.SUCCESS)
                .build();
        userService.recordAuditLog(auditLog);
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
    public void recordTokenRevoke(String userId, String username, String clientId,
                                  String ipAddress, String requestUri) {
        SecurityUserAuthAuditLog auditLog = SecurityUserAuthAuditLog.builder()
                .userId(userId)
                .username(username)
                .clientId(clientId)
                .operation(SecurityOperationEnum.TOKEN_REVOKE)
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .requestMethod(RequestMethodEnum.POST)
                .status(StatusEnum.SUCCESS)
                .build();
        userService.recordAuditLog(auditLog);
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
    public void recordAuthorizationCodeGenerate(String userId, String username, String clientId,
                                                String ipAddress, String requestUri) {
        SecurityUserAuthAuditLog auditLog = SecurityUserAuthAuditLog.builder()
                .userId(userId)
                .username(username)
                .clientId(clientId)
                .operation(SecurityOperationEnum.AUTHORIZATION_CODE_GENERATE)
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .requestMethod(RequestMethodEnum.GET)
                .status(StatusEnum.SUCCESS)
                .build();
        userService.recordAuditLog(auditLog);
    }

    /**
     * 记录客户端认证失败审计日志
     *
     * @param clientId     客户端ID
     * @param ipAddress    IP地址
     * @param requestUri   请求URI
     * @param errorMessage 错误消息
     */
    public void recordClientAuthFailure(String clientId, String ipAddress,
                                        String requestUri, String errorMessage) {
        SecurityUserAuthAuditLog auditLog = SecurityUserAuthAuditLog.builder()
                .clientId(clientId)
                .operation(SecurityOperationEnum.CLIENT_AUTH_FAILURE)
                .ipAddress(ipAddress)
                .requestUri(requestUri)
                .requestMethod(RequestMethodEnum.POST)
                .status(StatusEnum.FAILURE)
                .errorMessage(errorMessage)
                .build();
        userService.recordAuditLog(auditLog);
    }

    public void registerDevice(String userId, String deviceId, String deviceName, String deviceType, String ipAddress, UserAgent userAgent) {
        userService.registerDevice(SecurityUserDevice.builder()
                .userId(userId)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .deviceType(deviceType)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build());
    }

    public void updateLastLoginTime(String deviceId, LocalDateTime lastLoginTime) {
        userService.updateLastLoginTime(deviceId, lastLoginTime);
    }

    public boolean isPasswordInHistory(String userId, String password) {
        return userService.isPasswordInHistory(userId,password);
    }
}
