package com.ysmjjsy.goya.component.admin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ysmjjsy.goya.component.admin.permission.entity.IamPermissionEntity;
import com.ysmjjsy.goya.component.admin.permission.mapper.IamPermissionMapper;
import com.ysmjjsy.goya.component.admin.role.entity.IamRoleEntity;
import com.ysmjjsy.goya.component.admin.role.entity.IamRolePermissionEntity;
import com.ysmjjsy.goya.component.admin.role.entity.IamUserRoleEntity;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRoleMapper;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRolePermissionMapper;
import com.ysmjjsy.goya.component.admin.role.mapper.IamUserRoleMapper;
import com.ysmjjsy.goya.component.admin.support.AdminTenantSupport;
import com.ysmjjsy.goya.component.security.core.service.IRolePermissionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>角色权限 SPI 默认实现</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(IRolePermissionService.class)
public class AdminRolePermissionService implements IRolePermissionService {

    private final IamUserRoleMapper iamUserRoleMapper;
    private final IamRoleMapper iamRoleMapper;
    private final IamRolePermissionMapper iamRolePermissionMapper;
    private final IamPermissionMapper iamPermissionMapper;
    private final AdminTenantSupport adminTenantSupport;

    @Override
    public Set<String> findRolesByUserId(String userId) {
        String tenantId = adminTenantSupport.requiredTenantId(null);
        return findRolesByTenant(tenantId, userId);
    }

    @Override
    public Set<String> findPermissionsByUserId(String userId) {
        String tenantId = adminTenantSupport.requiredTenantId(null);
        if (StringUtils.isAnyBlank(tenantId, userId)) {
            return Set.of();
        }

        List<IamUserRoleEntity> userRoles = iamUserRoleMapper.selectList(new LambdaQueryWrapper<IamUserRoleEntity>()
                .eq(IamUserRoleEntity::getTenantId, tenantId)
                .eq(IamUserRoleEntity::getUserId, userId));
        List<String> roleIds = userRoles.stream().map(IamUserRoleEntity::getRoleId).filter(StringUtils::isNotBlank).distinct().toList();
        if (roleIds.isEmpty()) {
            return Set.of();
        }

        List<IamRolePermissionEntity> rolePermissions = iamRolePermissionMapper.selectList(new LambdaQueryWrapper<IamRolePermissionEntity>()
                .eq(IamRolePermissionEntity::getTenantId, tenantId)
                .in(IamRolePermissionEntity::getRoleId, roleIds));
        List<String> permissionIds = rolePermissions.stream().map(IamRolePermissionEntity::getPermissionId).filter(StringUtils::isNotBlank).distinct().toList();
        if (permissionIds.isEmpty()) {
            return Set.of();
        }

        List<IamPermissionEntity> permissions = iamPermissionMapper.selectList(new LambdaQueryWrapper<IamPermissionEntity>()
                .eq(IamPermissionEntity::getTenantId, tenantId)
                .in(IamPermissionEntity::getId, permissionIds));
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (IamPermissionEntity permission : permissions) {
            if (permission == null || StringUtils.isBlank(permission.getPermissionCode())) {
                continue;
            }
            result.add(permission.getPermissionCode());
        }
        return result;
    }

    @Override
    public Set<String> findRolesByTenant(String tenantId, String userId) {
        if (StringUtils.isAnyBlank(tenantId, userId)) {
            return Set.of();
        }
        List<IamUserRoleEntity> userRoles = iamUserRoleMapper.selectList(new LambdaQueryWrapper<IamUserRoleEntity>()
                .eq(IamUserRoleEntity::getTenantId, tenantId)
                .eq(IamUserRoleEntity::getUserId, userId));
        List<String> roleIds = userRoles.stream().map(IamUserRoleEntity::getRoleId).filter(StringUtils::isNotBlank).distinct().toList();
        if (roleIds.isEmpty()) {
            return Set.of();
        }

        List<IamRoleEntity> roles = iamRoleMapper.selectList(new LambdaQueryWrapper<IamRoleEntity>()
                .eq(IamRoleEntity::getTenantId, tenantId)
                .in(IamRoleEntity::getId, roleIds));
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (IamRoleEntity role : roles) {
            if (role == null || StringUtils.isBlank(role.getRoleCode())) {
                continue;
            }
            result.add(role.getRoleCode());
        }
        return result;
    }
}
