package com.ysmjjsy.goya.component.admin.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ysmjjsy.goya.component.admin.dept.entity.IamDeptEntity;
import com.ysmjjsy.goya.component.admin.dept.mapper.IamDeptMapper;
import com.ysmjjsy.goya.component.admin.error.AdminErrorCode;
import com.ysmjjsy.goya.component.admin.menu.entity.IamMenuEntity;
import com.ysmjjsy.goya.component.admin.menu.mapper.IamMenuMapper;
import com.ysmjjsy.goya.component.admin.permission.entity.IamPermissionEntity;
import com.ysmjjsy.goya.component.admin.permission.mapper.IamPermissionMapper;
import com.ysmjjsy.goya.component.admin.policy.service.AdminPolicyDomainService;
import com.ysmjjsy.goya.component.admin.role.dto.RoleDataScopeRequest;
import com.ysmjjsy.goya.component.admin.role.dto.RoleMenuAssignRequest;
import com.ysmjjsy.goya.component.admin.role.dto.RolePermissionAssignRequest;
import com.ysmjjsy.goya.component.admin.role.dto.RoleSaveRequest;
import com.ysmjjsy.goya.component.admin.role.entity.IamRoleDeptEntity;
import com.ysmjjsy.goya.component.admin.role.entity.IamRoleEntity;
import com.ysmjjsy.goya.component.admin.role.entity.IamRoleMenuEntity;
import com.ysmjjsy.goya.component.admin.role.entity.IamRolePermissionEntity;
import com.ysmjjsy.goya.component.admin.role.entity.IamUserRoleEntity;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRoleDeptMapper;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRoleMapper;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRoleMenuMapper;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRolePermissionMapper;
import com.ysmjjsy.goya.component.admin.role.mapper.IamUserRoleMapper;
import com.ysmjjsy.goya.component.admin.support.AdminTenantSupport;
import com.ysmjjsy.goya.component.admin.support.SessionRevokeSupport;
import com.ysmjjsy.goya.component.framework.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>角色管理领域服务</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Service
@RequiredArgsConstructor
public class AdminRoleDomainService {

    private final IamRoleMapper iamRoleMapper;
    private final IamPermissionMapper iamPermissionMapper;
    private final IamMenuMapper iamMenuMapper;
    private final IamDeptMapper iamDeptMapper;
    private final IamUserRoleMapper iamUserRoleMapper;
    private final IamRolePermissionMapper iamRolePermissionMapper;
    private final IamRoleMenuMapper iamRoleMenuMapper;
    private final IamRoleDeptMapper iamRoleDeptMapper;
    private final AdminTenantSupport adminTenantSupport;
    private final SessionRevokeSupport sessionRevokeSupport;
    private final ObjectProvider<AdminPolicyDomainService> adminPolicyDomainServiceProvider;

    public List<IamRoleEntity> list(String tenantId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        return iamRoleMapper.selectList(new LambdaQueryWrapper<IamRoleEntity>()
                .eq(IamRoleEntity::getTenantId, resolvedTenantId)
                .orderByAsc(IamRoleEntity::getRoleCode));
    }

    public IamRoleEntity detail(String tenantId, String roleId) {
        return requiredRole(tenantId, roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamRoleEntity create(RoleSaveRequest request) {
        if (request == null || StringUtils.isBlank(request.roleCode()) || StringUtils.isBlank(request.roleName())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "角色编码和名称不能为空");
        }
        String tenantId = adminTenantSupport.requiredTenantId(request.tenantId());
        ensureRoleCodeUnique(tenantId, request.roleCode(), null);

        IamRoleEntity entity = new IamRoleEntity();
        entity.setTenantId(tenantId);
        entity.setRoleCode(request.roleCode().trim());
        entity.setRoleName(request.roleName());
        entity.setDataScope(request.dataScope());
        entity.setStatus(request.status());
        entity.setRemark(request.remark());
        iamRoleMapper.insert(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public IamRoleEntity update(String roleId, RoleSaveRequest request) {
        IamRoleEntity existing = requiredRole(request == null ? null : request.tenantId(), roleId);
        if (request == null) {
            return existing;
        }
        if (StringUtils.isNotBlank(request.roleCode())) {
            ensureRoleCodeUnique(existing.getTenantId(), request.roleCode(), existing.getId());
            existing.setRoleCode(request.roleCode().trim());
        }
        if (StringUtils.isNotBlank(request.roleName())) {
            existing.setRoleName(request.roleName());
        }
        if (StringUtils.isNotBlank(request.dataScope())) {
            existing.setDataScope(request.dataScope());
        }
        if (StringUtils.isNotBlank(request.status())) {
            existing.setStatus(request.status());
        }
        existing.setRemark(request.remark());
        iamRoleMapper.updateById(existing);
        return existing;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String tenantId, String roleId) {
        IamRoleEntity existing = requiredRole(tenantId, roleId);
        iamRoleMapper.deleteById(existing.getId());
        iamUserRoleMapper.delete(new LambdaQueryWrapper<IamUserRoleEntity>()
                .eq(IamUserRoleEntity::getTenantId, existing.getTenantId())
                .eq(IamUserRoleEntity::getRoleId, existing.getId()));
        iamRolePermissionMapper.delete(new LambdaQueryWrapper<IamRolePermissionEntity>()
                .eq(IamRolePermissionEntity::getTenantId, existing.getTenantId())
                .eq(IamRolePermissionEntity::getRoleId, existing.getId()));
        iamRoleMenuMapper.delete(new LambdaQueryWrapper<IamRoleMenuEntity>()
                .eq(IamRoleMenuEntity::getTenantId, existing.getTenantId())
                .eq(IamRoleMenuEntity::getRoleId, existing.getId()));
        iamRoleDeptMapper.delete(new LambdaQueryWrapper<IamRoleDeptEntity>()
                .eq(IamRoleDeptEntity::getTenantId, existing.getTenantId())
                .eq(IamRoleDeptEntity::getRoleId, existing.getId()));

        syncRolePolicies(existing.getTenantId(), existing.getId(), existing.getRoleCode());
        revokeRoleUsers(existing.getTenantId(), existing.getId());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean assignPermissions(String roleId, RolePermissionAssignRequest request) {
        IamRoleEntity role = requiredRole(request == null ? null : request.tenantId(), roleId);
        iamRolePermissionMapper.delete(new LambdaQueryWrapper<IamRolePermissionEntity>()
                .eq(IamRolePermissionEntity::getTenantId, role.getTenantId())
                .eq(IamRolePermissionEntity::getRoleId, role.getId()));

        List<String> permissionIds = request == null || request.permissionIds() == null
                ? Collections.emptyList() : request.permissionIds();
        for (String permissionId : permissionIds) {
            if (StringUtils.isBlank(permissionId)) {
                continue;
            }
            ensurePermissionExists(role.getTenantId(), permissionId);
            IamRolePermissionEntity relation = new IamRolePermissionEntity();
            relation.setTenantId(role.getTenantId());
            relation.setRoleId(role.getId());
            relation.setPermissionId(permissionId);
            iamRolePermissionMapper.insert(relation);
        }

        syncRolePolicies(role.getTenantId(), role.getId(), role.getRoleCode());
        revokeRoleUsers(role.getTenantId(), role.getId());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean assignMenus(String roleId, RoleMenuAssignRequest request) {
        IamRoleEntity role = requiredRole(request == null ? null : request.tenantId(), roleId);
        iamRoleMenuMapper.delete(new LambdaQueryWrapper<IamRoleMenuEntity>()
                .eq(IamRoleMenuEntity::getTenantId, role.getTenantId())
                .eq(IamRoleMenuEntity::getRoleId, role.getId()));

        List<String> menuIds = request == null || request.menuIds() == null
                ? Collections.emptyList() : request.menuIds();
        for (String menuId : menuIds) {
            if (StringUtils.isBlank(menuId)) {
                continue;
            }
            ensureMenuExists(role.getTenantId(), menuId);
            IamRoleMenuEntity relation = new IamRoleMenuEntity();
            relation.setTenantId(role.getTenantId());
            relation.setRoleId(role.getId());
            relation.setMenuId(menuId);
            iamRoleMenuMapper.insert(relation);
        }
        revokeRoleUsers(role.getTenantId(), role.getId());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDataScope(String roleId, RoleDataScopeRequest request) {
        IamRoleEntity role = requiredRole(request == null ? null : request.tenantId(), roleId);
        if (request == null || StringUtils.isBlank(request.dataScope())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "dataScope 不能为空");
        }
        role.setDataScope(request.dataScope());
        iamRoleMapper.updateById(role);

        iamRoleDeptMapper.delete(new LambdaQueryWrapper<IamRoleDeptEntity>()
                .eq(IamRoleDeptEntity::getTenantId, role.getTenantId())
                .eq(IamRoleDeptEntity::getRoleId, role.getId()));

        List<String> deptIds = request.deptIds() == null ? Collections.emptyList() : request.deptIds();
        for (String deptId : deptIds) {
            if (StringUtils.isBlank(deptId)) {
                continue;
            }
            ensureDeptExists(role.getTenantId(), deptId);
            IamRoleDeptEntity relation = new IamRoleDeptEntity();
            relation.setTenantId(role.getTenantId());
            relation.setRoleId(role.getId());
            relation.setDeptId(deptId);
            iamRoleDeptMapper.insert(relation);
        }
        revokeRoleUsers(role.getTenantId(), role.getId());
        return true;
    }

    private IamRoleEntity requiredRole(String tenantId, String roleId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        IamRoleEntity role = iamRoleMapper.selectOne(new LambdaQueryWrapper<IamRoleEntity>()
                .eq(IamRoleEntity::getTenantId, resolvedTenantId)
                .eq(IamRoleEntity::getId, roleId));
        if (role == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private void ensureRoleCodeUnique(String tenantId, String roleCode, String excludeId) {
        LambdaQueryWrapper<IamRoleEntity> wrapper = new LambdaQueryWrapper<IamRoleEntity>()
                .eq(IamRoleEntity::getTenantId, tenantId)
                .eq(IamRoleEntity::getRoleCode, roleCode.trim());
        if (StringUtils.isNotBlank(excludeId)) {
            wrapper.ne(IamRoleEntity::getId, excludeId);
        }
        Long count = iamRoleMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(AdminErrorCode.ADMIN_UNIQUENESS_CONFLICT, "角色编码已存在");
        }
    }

    private void revokeRoleUsers(String tenantId, String roleId) {
        List<IamUserRoleEntity> userRoles = iamUserRoleMapper.selectList(new LambdaQueryWrapper<IamUserRoleEntity>()
                .eq(IamUserRoleEntity::getTenantId, tenantId)
                .eq(IamUserRoleEntity::getRoleId, roleId));
        List<String> userIds = userRoles.stream().map(IamUserRoleEntity::getUserId).distinct().collect(Collectors.toList());
        for (String userId : userIds) {
            sessionRevokeSupport.revokeByUserIfEnabled(tenantId, userId);
        }
    }

    private void syncRolePolicies(String tenantId, String roleId, String roleCode) {
        AdminPolicyDomainService policyDomainService = adminPolicyDomainServiceProvider.getIfAvailable();
        if (policyDomainService == null) {
            return;
        }
        policyDomainService.syncRolePermissionPolicies(tenantId, roleId, roleCode);
    }

    private void ensurePermissionExists(String tenantId, String permissionId) {
        Long count = iamPermissionMapper.selectCount(new LambdaQueryWrapper<IamPermissionEntity>()
                .eq(IamPermissionEntity::getTenantId, tenantId)
                .eq(IamPermissionEntity::getId, permissionId));
        if (count == null || count <= 0) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "权限不存在");
        }
    }

    private void ensureMenuExists(String tenantId, String menuId) {
        Long count = iamMenuMapper.selectCount(new LambdaQueryWrapper<IamMenuEntity>()
                .eq(IamMenuEntity::getTenantId, tenantId)
                .eq(IamMenuEntity::getId, menuId));
        if (count == null || count <= 0) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "菜单不存在");
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
