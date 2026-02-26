package com.ysmjjsy.goya.component.admin.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ysmjjsy.goya.component.admin.error.AdminErrorCode;
import com.ysmjjsy.goya.component.admin.permission.dto.PermissionSaveRequest;
import com.ysmjjsy.goya.component.admin.permission.entity.IamPermissionEntity;
import com.ysmjjsy.goya.component.admin.permission.mapper.IamPermissionMapper;
import com.ysmjjsy.goya.component.admin.role.entity.IamRolePermissionEntity;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRolePermissionMapper;
import com.ysmjjsy.goya.component.admin.support.AdminTenantSupport;
import com.ysmjjsy.goya.component.framework.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>权限管理领域服务</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Service
@RequiredArgsConstructor
public class AdminPermissionDomainService {

    private final IamPermissionMapper iamPermissionMapper;
    private final IamRolePermissionMapper iamRolePermissionMapper;
    private final AdminTenantSupport adminTenantSupport;

    public List<IamPermissionEntity> list(String tenantId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        return iamPermissionMapper.selectList(new LambdaQueryWrapper<IamPermissionEntity>()
                .eq(IamPermissionEntity::getTenantId, resolvedTenantId)
                .orderByAsc(IamPermissionEntity::getPermissionCode));
    }

    public IamPermissionEntity detail(String tenantId, String permissionId) {
        return requiredPermission(tenantId, permissionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamPermissionEntity create(PermissionSaveRequest request) {
        if (request == null || StringUtils.isBlank(request.permissionCode())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "permissionCode 不能为空");
        }
        String tenantId = adminTenantSupport.requiredTenantId(request.tenantId());
        ensurePermissionCodeUnique(tenantId, request.permissionCode(), null);

        IamPermissionEntity entity = new IamPermissionEntity();
        entity.setTenantId(tenantId);
        entity.setPermissionCode(request.permissionCode().trim());
        entity.setPermissionName(request.permissionName());
        entity.setResourceType(request.resourceType());
        entity.setResourceCode(request.resourceCode());
        entity.setActionCode(request.actionCode());
        entity.setStatus(request.status());
        entity.setRemark(request.remark());
        iamPermissionMapper.insert(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public IamPermissionEntity update(String permissionId, PermissionSaveRequest request) {
        IamPermissionEntity existing = requiredPermission(request == null ? null : request.tenantId(), permissionId);
        if (request == null) {
            return existing;
        }
        if (StringUtils.isNotBlank(request.permissionCode())) {
            ensurePermissionCodeUnique(existing.getTenantId(), request.permissionCode(), existing.getId());
            existing.setPermissionCode(request.permissionCode().trim());
        }
        if (StringUtils.isNotBlank(request.permissionName())) {
            existing.setPermissionName(request.permissionName());
        }
        existing.setResourceType(request.resourceType());
        existing.setResourceCode(request.resourceCode());
        existing.setActionCode(request.actionCode());
        existing.setStatus(request.status());
        existing.setRemark(request.remark());
        iamPermissionMapper.updateById(existing);
        return existing;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String tenantId, String permissionId) {
        IamPermissionEntity existing = requiredPermission(tenantId, permissionId);
        iamPermissionMapper.deleteById(existing.getId());
        iamRolePermissionMapper.delete(new LambdaQueryWrapper<IamRolePermissionEntity>()
                .eq(IamRolePermissionEntity::getTenantId, existing.getTenantId())
                .eq(IamRolePermissionEntity::getPermissionId, existing.getId()));
        return true;
    }

    private IamPermissionEntity requiredPermission(String tenantId, String permissionId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        IamPermissionEntity existing = iamPermissionMapper.selectOne(new LambdaQueryWrapper<IamPermissionEntity>()
                .eq(IamPermissionEntity::getTenantId, resolvedTenantId)
                .eq(IamPermissionEntity::getId, permissionId));
        if (existing == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "权限不存在");
        }
        return existing;
    }

    private void ensurePermissionCodeUnique(String tenantId, String permissionCode, String excludeId) {
        LambdaQueryWrapper<IamPermissionEntity> wrapper = new LambdaQueryWrapper<IamPermissionEntity>()
                .eq(IamPermissionEntity::getTenantId, tenantId)
                .eq(IamPermissionEntity::getPermissionCode, permissionCode.trim());
        if (StringUtils.isNotBlank(excludeId)) {
            wrapper.ne(IamPermissionEntity::getId, excludeId);
        }
        Long count = iamPermissionMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(AdminErrorCode.ADMIN_UNIQUENESS_CONFLICT, "权限编码已存在");
        }
    }
}
