package com.ysmjjsy.goya.component.security.core.service;

import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUserAuthAuditLog;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUserDevice;

import java.time.LocalDateTime;
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
     * @param userId userId
     * @return 用户
     */
    SecurityUser findUserByUserId(String userId);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    SecurityUser findUserByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phoneNumber 手机号
     * @return 用户
     */
    SecurityUser findUserByPhoneNumber(String phoneNumber);

    /**
     * 注册用户
     *
     * @param userPrincipal 用户信息
     * @return 用户信息
     */
    SecurityUser registerUser(SecurityUser userPrincipal);

    /**
     * 用户锁定
     *
     * @param userId 用户Id
     */
    void lockedUser(String userId);

    /**
     * 注册设备
     *
     * @param userDevice 用户设备信息
     * @return 设备信息
     */
    SecurityUserDevice registerDevice(SecurityUserDevice userDevice);

    /**
     * 根据用户ID查询设备列表
     *
     * @param userId 用户ID
     * @return 设备列表
     */
    List<SecurityUserDevice> findByUserId(String userId);

    /**
     * 根据设备ID查询设备
     *
     * @param deviceId 设备ID
     * @return 设备信息
     */
    SecurityUserDevice findByDeviceId(String deviceId);

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
     * @param lastLoginTime     最后登录时间
     */
    void updateLastLoginTime(String deviceId, LocalDateTime lastLoginTime);

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
    void recordAuditLog(SecurityUserAuthAuditLog auditLog);

    /**
     * 检查密码是否在历史密码中（防止重复使用）
     * <p>注意：此方法需要用户服务实现历史密码存储和检查逻辑</p>
     *
     * @param userId   用户名
     * @param password 新密码
     * @return true如果密码在历史中，false如果不在
     */
    boolean isPasswordInHistory(String userId, String password);
}
