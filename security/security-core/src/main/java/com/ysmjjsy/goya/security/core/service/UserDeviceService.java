package com.ysmjjsy.goya.security.core.service;

import com.ysmjjsy.goya.component.web.utils.UserAgent;
import com.ysmjjsy.goya.security.core.domain.SecurityUserDevice;

import java.util.List;

/**
 * <p>用户设备服务接口</p>
 * <p>提供设备注册、查询、信任、撤销接口</p>
 * <p>注意：此接口需要实现类来持久化设备信息（数据库、Redis等）</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface UserDeviceService {

    /**
     * 注册设备
     *
     * @param userId 用户ID
     * @param deviceId 设备ID（设备指纹）
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param ipAddress IP地址
     * @param userAgent User-Agent
     * @return 设备信息
     */
    SecurityUserDevice registerDevice(String userId, String deviceId, String deviceName,
                                      String deviceType, String ipAddress, UserAgent userAgent);

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
     */
    void updateLastLoginTime(String deviceId);

    /**
     * 删除设备
     *
     * @param deviceId 设备ID
     */
    void deleteDevice(String deviceId);
}

