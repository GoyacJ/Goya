package com.ysmjjsy.goya.component.admin.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ysmjjsy.goya.component.admin.enums.UserStatusEnum;
import com.ysmjjsy.goya.component.admin.error.AdminErrorCode;
import com.ysmjjsy.goya.component.admin.role.entity.IamRoleEntity;
import com.ysmjjsy.goya.component.admin.role.entity.IamUserRoleEntity;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRoleMapper;
import com.ysmjjsy.goya.component.admin.role.mapper.IamUserRoleMapper;
import com.ysmjjsy.goya.component.admin.support.AdminPasswordSupport;
import com.ysmjjsy.goya.component.admin.support.AdminTenantSupport;
import com.ysmjjsy.goya.component.admin.support.SessionRevokeSupport;
import com.ysmjjsy.goya.component.admin.dept.entity.IamDeptEntity;
import com.ysmjjsy.goya.component.admin.dept.entity.IamUserDeptEntity;
import com.ysmjjsy.goya.component.admin.dept.mapper.IamDeptMapper;
import com.ysmjjsy.goya.component.admin.dept.mapper.IamUserDeptMapper;
import com.ysmjjsy.goya.component.admin.user.dto.*;
import com.ysmjjsy.goya.component.admin.user.entity.IamUserDeviceEntity;
import com.ysmjjsy.goya.component.admin.user.entity.IamUserEntity;
import com.ysmjjsy.goya.component.admin.user.mapper.IamUserDeviceMapper;
import com.ysmjjsy.goya.component.admin.user.mapper.IamUserMapper;
import com.ysmjjsy.goya.component.framework.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * <p>用户管理领域服务</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Service
@RequiredArgsConstructor
public class AdminUserDomainService {

    private final IamUserMapper iamUserMapper;
    private final IamRoleMapper iamRoleMapper;
    private final IamUserRoleMapper iamUserRoleMapper;
    private final IamDeptMapper iamDeptMapper;
    private final IamUserDeptMapper iamUserDeptMapper;
    private final IamUserDeviceMapper iamUserDeviceMapper;
    private final AdminTenantSupport adminTenantSupport;
    private final AdminPasswordSupport adminPasswordSupport;
    private final SessionRevokeSupport sessionRevokeSupport;

    public List<IamUserEntity> list(String tenantId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        return iamUserMapper.selectList(new LambdaQueryWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, resolvedTenantId)
                .orderByDesc(IamUserEntity::getCreatedAt));
    }

    public IamUserEntity detail(String tenantId, String userId) {
        return requiredUser(tenantId, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamUserEntity create(UserSaveRequest request) {
        if (request == null || StringUtils.isBlank(request.username())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "用户名不能为空");
        }
        String tenantId = adminTenantSupport.requiredTenantId(request.tenantId());
        ensureUsernameUnique(tenantId, request.username(), null);

        IamUserEntity entity = new IamUserEntity();
        entity.setTenantId(tenantId);
        entity.setUsername(request.username().trim());
        entity.setNickname(request.nickname());
        entity.setPhoneNumber(request.phoneNumber());
        entity.setEmail(request.email());
        entity.setAvatar(request.avatar());
        entity.setOpenId(request.openId());
        entity.setStatus(StringUtils.defaultIfBlank(request.status(), UserStatusEnum.ENABLED.getCode()));
        if (StringUtils.isNotBlank(request.password())) {
            entity.setPassword(adminPasswordSupport.encode(request.password()));
            entity.setPasswordChangedAt(LocalDateTime.now());
        }
        iamUserMapper.insert(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public IamUserEntity update(String userId, UserSaveRequest request) {
        IamUserEntity existing = requiredUser(request == null ? null : request.tenantId(), userId);
        if (request == null) {
            return existing;
        }
        if (StringUtils.isNotBlank(request.username())) {
            ensureUsernameUnique(existing.getTenantId(), request.username(), existing.getId());
            existing.setUsername(request.username().trim());
        }
        if (StringUtils.isNotBlank(request.password())) {
            existing.setPassword(adminPasswordSupport.encode(request.password()));
            existing.setPasswordChangedAt(LocalDateTime.now());
        }
        existing.setNickname(request.nickname());
        existing.setPhoneNumber(request.phoneNumber());
        existing.setEmail(request.email());
        existing.setAvatar(request.avatar());
        existing.setOpenId(request.openId());
        if (StringUtils.isNotBlank(request.status())) {
            existing.setStatus(request.status());
        }
        iamUserMapper.updateById(existing);
        return existing;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String tenantId, String userId) {
        IamUserEntity existing = requiredUser(tenantId, userId);
        iamUserMapper.deleteById(existing.getId());
        iamUserRoleMapper.delete(new LambdaQueryWrapper<IamUserRoleEntity>()
                .eq(IamUserRoleEntity::getTenantId, existing.getTenantId())
                .eq(IamUserRoleEntity::getUserId, existing.getId()));
        iamUserDeptMapper.delete(new LambdaQueryWrapper<IamUserDeptEntity>()
                .eq(IamUserDeptEntity::getTenantId, existing.getTenantId())
                .eq(IamUserDeptEntity::getUserId, existing.getId()));
        sessionRevokeSupport.revokeByUserIfEnabled(existing.getTenantId(), existing.getId());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(String userId, UserStatusRequest request) {
        IamUserEntity existing = requiredUser(request == null ? null : request.tenantId(), userId);
        if (request == null || StringUtils.isBlank(request.status())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "status 不能为空");
        }
        existing.setStatus(request.status());
        iamUserMapper.updateById(existing);
        if (StringUtils.equalsAnyIgnoreCase(request.status(), UserStatusEnum.DISABLED.getCode(), UserStatusEnum.LOCKED.getCode())) {
            sessionRevokeSupport.revokeByUserIfEnabled(existing.getTenantId(), existing.getId());
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePassword(String userId, UserPasswordRequest request) {
        IamUserEntity existing = requiredUser(request == null ? null : request.tenantId(), userId);
        if (request == null || StringUtils.isBlank(request.password())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "password 不能为空");
        }
        existing.setPassword(adminPasswordSupport.encode(request.password()));
        existing.setPasswordChangedAt(LocalDateTime.now());
        iamUserMapper.updateById(existing);
        sessionRevokeSupport.revokeByUserIfEnabled(existing.getTenantId(), existing.getId());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean assignRoles(String userId, UserRoleAssignRequest request) {
        IamUserEntity existing = requiredUser(request == null ? null : request.tenantId(), userId);
        iamUserRoleMapper.delete(new LambdaQueryWrapper<IamUserRoleEntity>()
                .eq(IamUserRoleEntity::getTenantId, existing.getTenantId())
                .eq(IamUserRoleEntity::getUserId, existing.getId()));
        List<String> roleIds = request == null || request.roleIds() == null ? Collections.emptyList() : request.roleIds();
        for (String roleId : roleIds) {
            if (StringUtils.isBlank(roleId)) {
                continue;
            }
            ensureRoleExists(existing.getTenantId(), roleId);
            IamUserRoleEntity relation = new IamUserRoleEntity();
            relation.setTenantId(existing.getTenantId());
            relation.setUserId(existing.getId());
            relation.setRoleId(roleId);
            iamUserRoleMapper.insert(relation);
        }
        sessionRevokeSupport.revokeByUserIfEnabled(existing.getTenantId(), existing.getId());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean assignDepts(String userId, UserDeptAssignRequest request) {
        IamUserEntity existing = requiredUser(request == null ? null : request.tenantId(), userId);
        iamUserDeptMapper.delete(new LambdaQueryWrapper<IamUserDeptEntity>()
                .eq(IamUserDeptEntity::getTenantId, existing.getTenantId())
                .eq(IamUserDeptEntity::getUserId, existing.getId()));
        List<String> deptIds = request == null || request.deptIds() == null ? Collections.emptyList() : request.deptIds();
        for (String deptId : deptIds) {
            if (StringUtils.isBlank(deptId)) {
                continue;
            }
            ensureDeptExists(existing.getTenantId(), deptId);
            IamUserDeptEntity relation = new IamUserDeptEntity();
            relation.setTenantId(existing.getTenantId());
            relation.setUserId(existing.getId());
            relation.setDeptId(deptId);
            iamUserDeptMapper.insert(relation);
        }
        return true;
    }

    public List<IamUserDeviceEntity> devices(String tenantId, String userId) {
        IamUserEntity existing = requiredUser(tenantId, userId);
        return iamUserDeviceMapper.selectList(new LambdaQueryWrapper<IamUserDeviceEntity>()
                .eq(IamUserDeviceEntity::getTenantId, existing.getTenantId())
                .eq(IamUserDeviceEntity::getUserId, existing.getId())
                .orderByDesc(IamUserDeviceEntity::getLastLoginTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean removeDevice(String tenantId, String userId, String deviceId) {
        IamUserEntity existing = requiredUser(tenantId, userId);
        LambdaQueryWrapper<IamUserDeviceEntity> queryWrapper = new LambdaQueryWrapper<IamUserDeviceEntity>()
                .eq(IamUserDeviceEntity::getTenantId, existing.getTenantId())
                .eq(IamUserDeviceEntity::getUserId, existing.getId());
        if (StringUtils.isNotBlank(deviceId)) {
            queryWrapper.eq(IamUserDeviceEntity::getDeviceId, deviceId);
        }
        iamUserDeviceMapper.delete(queryWrapper);
        return true;
    }

    private IamUserEntity requiredUser(String tenantId, String userId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        IamUserEntity existing = iamUserMapper.selectOne(new LambdaQueryWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, resolvedTenantId)
                .eq(IamUserEntity::getId, userId));
        if (existing == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "用户不存在");
        }
        return existing;
    }

    private void ensureUsernameUnique(String tenantId, String username, String excludeId) {
        LambdaQueryWrapper<IamUserEntity> queryWrapper = new LambdaQueryWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getUsername, username.trim());
        if (StringUtils.isNotBlank(excludeId)) {
            queryWrapper.ne(IamUserEntity::getId, excludeId);
        }
        Long count = iamUserMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(AdminErrorCode.ADMIN_UNIQUENESS_CONFLICT, "用户名已存在");
        }
    }

    private void ensureRoleExists(String tenantId, String roleId) {
        Long count = iamRoleMapper.selectCount(new LambdaQueryWrapper<IamRoleEntity>()
                .eq(IamRoleEntity::getTenantId, tenantId)
                .eq(IamRoleEntity::getId, roleId));
        if (count == null || count <= 0) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "角色不存在");
        }
    }

    private void ensureDeptExists(String tenantId, String deptId) {
        Long count = iamDeptMapper.selectCount(new LambdaQueryWrapper<IamDeptEntity>()
                .eq(IamDeptEntity::getTenantId, tenantId)
                .eq(IamDeptEntity::getId, deptId));
        if (count == null || count <= 0) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "部门不存在");
        }
    }
}
