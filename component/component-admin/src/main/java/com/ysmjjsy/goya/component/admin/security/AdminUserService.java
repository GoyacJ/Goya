package com.ysmjjsy.goya.component.admin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ysmjjsy.goya.component.admin.enums.UserStatusEnum;
import com.ysmjjsy.goya.component.admin.role.entity.IamRoleEntity;
import com.ysmjjsy.goya.component.admin.role.entity.IamUserRoleEntity;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRoleMapper;
import com.ysmjjsy.goya.component.admin.role.mapper.IamUserRoleMapper;
import com.ysmjjsy.goya.component.admin.support.AdminTenantSupport;
import com.ysmjjsy.goya.component.admin.user.entity.IamUserAuthAuditLogEntity;
import com.ysmjjsy.goya.component.admin.user.entity.IamUserDeviceEntity;
import com.ysmjjsy.goya.component.admin.user.entity.IamUserEntity;
import com.ysmjjsy.goya.component.admin.user.entity.IamUserPasswordHistoryEntity;
import com.ysmjjsy.goya.component.admin.user.mapper.IamUserAuthAuditLogMapper;
import com.ysmjjsy.goya.component.admin.user.mapper.IamUserDeviceMapper;
import com.ysmjjsy.goya.component.admin.user.mapper.IamUserMapper;
import com.ysmjjsy.goya.component.admin.user.mapper.IamUserPasswordHistoryMapper;
import com.ysmjjsy.goya.component.security.core.domain.SecurityGrantedAuthority;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUserAuthAuditLog;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUserDevice;
import com.ysmjjsy.goya.component.security.core.enums.SecurityOperationEnum;
import com.ysmjjsy.goya.component.security.core.service.IRolePermissionService;
import com.ysmjjsy.goya.component.security.core.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>用户 SPI 默认实现</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(IUserService.class)
public class AdminUserService implements IUserService {

    private final IamUserMapper iamUserMapper;
    private final IamUserDeviceMapper iamUserDeviceMapper;
    private final IamUserAuthAuditLogMapper iamUserAuthAuditLogMapper;
    private final IamUserPasswordHistoryMapper iamUserPasswordHistoryMapper;
    private final IamUserRoleMapper iamUserRoleMapper;
    private final IamRoleMapper iamRoleMapper;
    private final IRolePermissionService rolePermissionService;
    private final AdminTenantSupport adminTenantSupport;

    @Override
    public SecurityUser findUserByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        IamUserEntity user = iamUserMapper.selectOne(new LambdaQueryWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getId, userId));
        return toSecurityUser(user);
    }

    @Override
    public SecurityUser findUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        IamUserEntity user = iamUserMapper.selectOne(new LambdaQueryWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getUsername, username));
        return toSecurityUser(user);
    }

    @Override
    public SecurityUser findUserByPhoneNumber(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            return null;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        IamUserEntity user = iamUserMapper.selectOne(new LambdaQueryWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getPhoneNumber, phoneNumber));
        return toSecurityUser(user);
    }

    @Override
    public SecurityUser findUserByEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return null;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        IamUserEntity user = iamUserMapper.selectOne(new LambdaQueryWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getEmail, email));
        return toSecurityUser(user);
    }

    @Override
    public SecurityUser registerUser(SecurityUser userPrincipal) {
        if (userPrincipal == null || StringUtils.isBlank(userPrincipal.getUsername())) {
            return null;
        }
        String tenantId = adminTenantSupport.requiredTenantId(userPrincipal.getTenantId());
        IamUserEntity entity = new IamUserEntity();
        entity.setTenantId(tenantId);
        entity.setUsername(userPrincipal.getUsername());
        entity.setPassword(userPrincipal.getPassword());
        entity.setNickname(userPrincipal.getNickname());
        entity.setPhoneNumber(userPrincipal.getPhoneNumber());
        entity.setEmail(userPrincipal.getEmail());
        entity.setAvatar(userPrincipal.getAvatar());
        entity.setOpenId(userPrincipal.getOpenId());
        entity.setStatus(UserStatusEnum.ENABLED.getCode());
        entity.setPasswordChangedAt(LocalDateTime.now());
        iamUserMapper.insert(entity);

        if (userPrincipal.getRoles() != null) {
            bindRolesByCodes(tenantId, entity.getId(), userPrincipal.getRoles());
        }
        return toSecurityUser(entity);
    }

    @Override
    public void lockedUser(String userId) {
        updateUserStatus(userId, UserStatusEnum.LOCKED.getCode());
    }

    @Override
    public void unlockUser(String userId) {
        updateUserStatus(userId, UserStatusEnum.ENABLED.getCode());
    }

    @Override
    public SecurityUserDevice registerDevice(SecurityUserDevice userDevice) {
        if (userDevice == null || StringUtils.isAnyBlank(userDevice.getUserId(), userDevice.getDeviceId())) {
            return null;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        IamUserDeviceEntity existing = iamUserDeviceMapper.selectOne(new LambdaQueryWrapper<IamUserDeviceEntity>()
                .eq(IamUserDeviceEntity::getTenantId, tenantId)
                .eq(IamUserDeviceEntity::getUserId, userDevice.getUserId())
                .eq(IamUserDeviceEntity::getDeviceId, userDevice.getDeviceId()));
        if (existing == null) {
            existing = new IamUserDeviceEntity();
            existing.setTenantId(tenantId);
            existing.setUserId(userDevice.getUserId());
            existing.setDeviceId(userDevice.getDeviceId());
            existing.setDeviceName(userDevice.getDeviceName());
            existing.setDeviceType(userDevice.getDeviceType());
            existing.setTrusted(Boolean.TRUE.equals(userDevice.getTrusted()));
            existing.setIpAddress(userDevice.getIpAddress());
            existing.setUserAgent(userDevice.getUserAgent() == null ? null : userDevice.getUserAgent().toString());
            existing.setLastLoginTime(userDevice.getLastLoginTime());
            iamUserDeviceMapper.insert(existing);
        } else {
            existing.setDeviceName(userDevice.getDeviceName());
            existing.setDeviceType(userDevice.getDeviceType());
            existing.setTrusted(Boolean.TRUE.equals(userDevice.getTrusted()));
            existing.setIpAddress(userDevice.getIpAddress());
            existing.setUserAgent(userDevice.getUserAgent() == null ? null : userDevice.getUserAgent().toString());
            existing.setLastLoginTime(userDevice.getLastLoginTime());
            iamUserDeviceMapper.updateById(existing);
        }
        return toSecurityUserDevice(existing);
    }

    @Override
    public List<SecurityUserDevice> findByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return List.of();
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        List<IamUserDeviceEntity> devices = iamUserDeviceMapper.selectList(new LambdaQueryWrapper<IamUserDeviceEntity>()
                .eq(IamUserDeviceEntity::getTenantId, tenantId)
                .eq(IamUserDeviceEntity::getUserId, userId)
                .orderByDesc(IamUserDeviceEntity::getLastLoginTime));
        List<SecurityUserDevice> result = new ArrayList<>();
        for (IamUserDeviceEntity device : devices) {
            result.add(toSecurityUserDevice(device));
        }
        return result;
    }

    @Override
    public SecurityUserDevice findByDeviceId(String deviceId) {
        if (StringUtils.isBlank(deviceId)) {
            return null;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        IamUserDeviceEntity device = iamUserDeviceMapper.selectOne(new LambdaQueryWrapper<IamUserDeviceEntity>()
                .eq(IamUserDeviceEntity::getTenantId, tenantId)
                .eq(IamUserDeviceEntity::getDeviceId, deviceId)
                .last("LIMIT 1"));
        return toSecurityUserDevice(device);
    }

    @Override
    public void trustDevice(String deviceId) {
        updateDeviceTrust(deviceId, true);
    }

    @Override
    public void revokeDevice(String deviceId) {
        updateDeviceTrust(deviceId, false);
    }

    @Override
    public void updateLastLoginTime(String deviceId, LocalDateTime lastLoginTime) {
        String tenantId = adminTenantSupport.requiredTenantId(null);
        iamUserDeviceMapper.update(null, new LambdaUpdateWrapper<IamUserDeviceEntity>()
                .eq(IamUserDeviceEntity::getTenantId, tenantId)
                .eq(IamUserDeviceEntity::getDeviceId, deviceId)
                .set(IamUserDeviceEntity::getLastLoginTime, lastLoginTime));
    }

    @Override
    public void deleteDevice(String deviceId) {
        String tenantId = adminTenantSupport.requiredTenantId(null);
        iamUserDeviceMapper.delete(new LambdaQueryWrapper<IamUserDeviceEntity>()
                .eq(IamUserDeviceEntity::getTenantId, tenantId)
                .eq(IamUserDeviceEntity::getDeviceId, deviceId));
    }

    @Override
    public void recordAuditLog(SecurityUserAuthAuditLog auditLog) {
        if (auditLog == null) {
            return;
        }
        String tenantId = adminTenantSupport.requiredTenantId(auditLog.getTenantId());
        IamUserAuthAuditLogEntity entity = new IamUserAuthAuditLogEntity();
        entity.setTenantId(tenantId);
        entity.setUserId(auditLog.getUserId());
        entity.setUsername(auditLog.getUsername());
        SecurityOperationEnum operation = auditLog.getOperation();
        entity.setOperation(operation == null ? null : operation.getCode());
        entity.setIpAddress(auditLog.getIpAddress());
        entity.setUserAgent(auditLog.getUserAgent() == null ? null : auditLog.getUserAgent().toString());
        entity.setRequestUri(auditLog.getRequestUri());
        entity.setRequestMethod(auditLog.getRequestMethod() == null ? null : auditLog.getRequestMethod().getCode());
        entity.setStatus(auditLog.getStatus() == null ? null : auditLog.getStatus().getCode());
        entity.setErrorMessage(auditLog.getErrorMessage());
        entity.setClientId(auditLog.getClientId());
        entity.setTimestamp(auditLog.getTimestamp() == null ? LocalDateTime.now() : auditLog.getTimestamp());
        iamUserAuthAuditLogMapper.insert(entity);
    }

    @Override
    public boolean isPasswordInHistory(String userId, String password) {
        if (StringUtils.isAnyBlank(userId, password)) {
            return false;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        Long count = iamUserPasswordHistoryMapper.selectCount(new LambdaQueryWrapper<IamUserPasswordHistoryEntity>()
                .eq(IamUserPasswordHistoryEntity::getTenantId, tenantId)
                .eq(IamUserPasswordHistoryEntity::getUserId, userId)
                .eq(IamUserPasswordHistoryEntity::getPassword, password));
        return count != null && count > 0;
    }

    @Override
    public void updatePassword(String userId, String newPassword) {
        if (StringUtils.isAnyBlank(userId, newPassword)) {
            return;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        iamUserMapper.update(null, new LambdaUpdateWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getId, userId)
                .set(IamUserEntity::getPassword, newPassword)
                .set(IamUserEntity::getPasswordChangedAt, LocalDateTime.now()));

        IamUserPasswordHistoryEntity historyEntity = new IamUserPasswordHistoryEntity();
        historyEntity.setTenantId(tenantId);
        historyEntity.setUserId(userId);
        historyEntity.setPassword(newPassword);
        iamUserPasswordHistoryMapper.insert(historyEntity);
    }

    @Override
    public LocalDateTime getPasswordLastModifiedTime(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        IamUserEntity entity = iamUserMapper.selectOne(new LambdaQueryWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getId, userId));
        return entity == null ? null : entity.getPasswordChangedAt();
    }

    @Override
    public void updateNickname(String userId, String nickname) {
        updateSimpleProfileField(userId, "nickname", nickname);
    }

    @Override
    public void updateAvatar(String userId, String avatar) {
        updateSimpleProfileField(userId, "avatar", avatar);
    }

    @Override
    public void updateEmail(String userId, String email) {
        if (StringUtils.isBlank(userId)) {
            return;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        if (StringUtils.isNotBlank(email)) {
            Long count = iamUserMapper.selectCount(new LambdaQueryWrapper<IamUserEntity>()
                    .eq(IamUserEntity::getTenantId, tenantId)
                    .eq(IamUserEntity::getEmail, email)
                    .ne(IamUserEntity::getId, userId));
            if (count != null && count > 0) {
                return;
            }
        }
        iamUserMapper.update(null, new LambdaUpdateWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getId, userId)
                .set(IamUserEntity::getEmail, email));
    }

    @Override
    public void updatePhoneNumber(String userId, String phoneNumber) {
        if (StringUtils.isBlank(userId)) {
            return;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        if (StringUtils.isNotBlank(phoneNumber)) {
            Long count = iamUserMapper.selectCount(new LambdaQueryWrapper<IamUserEntity>()
                    .eq(IamUserEntity::getTenantId, tenantId)
                    .eq(IamUserEntity::getPhoneNumber, phoneNumber)
                    .ne(IamUserEntity::getId, userId));
            if (count != null && count > 0) {
                return;
            }
        }
        iamUserMapper.update(null, new LambdaUpdateWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getId, userId)
                .set(IamUserEntity::getPhoneNumber, phoneNumber));
    }

    private void bindRolesByCodes(String tenantId, String userId, Set<String> roleCodes) {
        if (StringUtils.isAnyBlank(tenantId, userId) || roleCodes == null || roleCodes.isEmpty()) {
            return;
        }
        List<IamRoleEntity> roles = iamRoleMapper.selectList(new LambdaQueryWrapper<IamRoleEntity>()
                .eq(IamRoleEntity::getTenantId, tenantId)
                .in(IamRoleEntity::getRoleCode, roleCodes));
        for (IamRoleEntity role : roles) {
            if (role == null || StringUtils.isBlank(role.getId())) {
                continue;
            }
            IamUserRoleEntity relation = new IamUserRoleEntity();
            relation.setTenantId(tenantId);
            relation.setUserId(userId);
            relation.setRoleId(role.getId());
            iamUserRoleMapper.insert(relation);
        }
    }

    private void updateUserStatus(String userId, String status) {
        if (StringUtils.isAnyBlank(userId, status)) {
            return;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        iamUserMapper.update(null, new LambdaUpdateWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getId, userId)
                .set(IamUserEntity::getStatus, status));
    }

    private void updateDeviceTrust(String deviceId, boolean trusted) {
        if (StringUtils.isBlank(deviceId)) {
            return;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        iamUserDeviceMapper.update(null, new LambdaUpdateWrapper<IamUserDeviceEntity>()
                .eq(IamUserDeviceEntity::getTenantId, tenantId)
                .eq(IamUserDeviceEntity::getDeviceId, deviceId)
                .set(IamUserDeviceEntity::getTrusted, trusted));
    }

    private void updateSimpleProfileField(String userId, String field, String value) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(field)) {
            return;
        }
        String tenantId = adminTenantSupport.requiredTenantId(null);
        LambdaUpdateWrapper<IamUserEntity> wrapper = new LambdaUpdateWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getId, userId);
        if ("nickname".equals(field)) {
            wrapper.set(IamUserEntity::getNickname, value);
        } else if ("avatar".equals(field)) {
            wrapper.set(IamUserEntity::getAvatar, value);
        }
        iamUserMapper.update(null, wrapper);
    }

    private SecurityUser toSecurityUser(IamUserEntity user) {
        if (user == null) {
            return null;
        }
        Set<String> roles = rolePermissionService.findRolesByTenant(user.getTenantId(), user.getId());
        if (roles == null) {
            roles = Set.of();
        }
        Set<String> permissions = rolePermissionService.findPermissionsByUserId(user.getId());
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (String permission : permissions) {
            if (StringUtils.isNotBlank(permission)) {
                authorities.add(new SecurityGrantedAuthority(permission));
            }
        }
        for (String role : roles) {
            if (StringUtils.isNotBlank(role)) {
                authorities.add(new SecurityGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
            }
        }

        boolean locked = StringUtils.equalsIgnoreCase(user.getStatus(), UserStatusEnum.LOCKED.getCode());
        boolean enabled = !StringUtils.equalsIgnoreCase(user.getStatus(), UserStatusEnum.DISABLED.getCode());

        return SecurityUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .password(StringUtils.defaultString(user.getPassword()))
                .openId(user.getOpenId())
                .tenantId(user.getTenantId())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(roles)
                .authorities(authorities)
                .enabled(enabled)
                .accountNonExpired(true)
                .accountNonLocked(!locked)
                .credentialsNonExpired(true)
                .build();
    }

    private SecurityUserDevice toSecurityUserDevice(IamUserDeviceEntity entity) {
        if (entity == null) {
            return null;
        }
        return SecurityUserDevice.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .deviceId(entity.getDeviceId())
                .deviceName(entity.getDeviceName())
                .deviceType(entity.getDeviceType())
                .trusted(entity.getTrusted())
                .lastLoginTime(entity.getLastLoginTime())
                .createdAt(entity.getCreatedAt() == null ? null : LocalDateTime.ofInstant(entity.getCreatedAt(), java.time.ZoneOffset.UTC))
                .ipAddress(entity.getIpAddress())
                .build();
    }
}
