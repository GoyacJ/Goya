package com.ysmjjsy.goya.component.admin.dept.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ysmjjsy.goya.component.admin.dept.dto.DeptSaveRequest;
import com.ysmjjsy.goya.component.admin.dept.entity.IamDeptEntity;
import com.ysmjjsy.goya.component.admin.dept.entity.IamUserDeptEntity;
import com.ysmjjsy.goya.component.admin.dept.mapper.IamDeptMapper;
import com.ysmjjsy.goya.component.admin.dept.mapper.IamUserDeptMapper;
import com.ysmjjsy.goya.component.admin.error.AdminErrorCode;
import com.ysmjjsy.goya.component.admin.role.entity.IamRoleDeptEntity;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRoleDeptMapper;
import com.ysmjjsy.goya.component.admin.support.AdminTenantSupport;
import com.ysmjjsy.goya.component.admin.support.AdminTreeSupport;
import com.ysmjjsy.goya.component.framework.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * <p>部门管理领域服务</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Service
@RequiredArgsConstructor
public class AdminDeptDomainService {

    private final IamDeptMapper iamDeptMapper;
    private final IamUserDeptMapper iamUserDeptMapper;
    private final IamRoleDeptMapper iamRoleDeptMapper;
    private final AdminTenantSupport adminTenantSupport;
    private final AdminTreeSupport adminTreeSupport;

    public List<IamDeptEntity> tree(String tenantId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        List<IamDeptEntity> list = iamDeptMapper.selectList(new LambdaQueryWrapper<IamDeptEntity>()
                .eq(IamDeptEntity::getTenantId, resolvedTenantId)
                .orderByAsc(IamDeptEntity::getSort)
                .orderByAsc(IamDeptEntity::getDeptCode));
        list.sort(Comparator.comparing(item -> item.getSort() == null ? Integer.MAX_VALUE : item.getSort()));
        return adminTreeSupport.buildTree(list);
    }

    public IamDeptEntity detail(String tenantId, String deptId) {
        return requiredDept(tenantId, deptId);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDeptEntity create(DeptSaveRequest request) {
        if (request == null || StringUtils.isBlank(request.deptCode()) || StringUtils.isBlank(request.deptName())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "deptCode/deptName 不能为空");
        }
        String tenantId = adminTenantSupport.requiredTenantId(request.tenantId());
        ensureDeptCodeUnique(tenantId, request.deptCode(), null);

        IamDeptEntity entity = new IamDeptEntity();
        entity.setTenantId(tenantId);
        entity.setDeptCode(request.deptCode().trim());
        entity.setDeptName(request.deptName());
        entity.setLeader(request.leader());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setSort(request.sort());
        entity.setStatus(request.status());

        String parentId = adminTreeSupport.normalizeParentId(request.parentId());
        entity.setParentId(parentId);
        entity.setParentIds(adminTreeSupport.resolveParentIds(parentId, value -> queryParentIds(tenantId, value)));
        iamDeptMapper.insert(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDeptEntity update(String deptId, DeptSaveRequest request) {
        IamDeptEntity existing = requiredDept(request == null ? null : request.tenantId(), deptId);
        if (request == null) {
            return existing;
        }

        if (StringUtils.isNotBlank(request.deptCode())) {
            ensureDeptCodeUnique(existing.getTenantId(), request.deptCode(), existing.getId());
            existing.setDeptCode(request.deptCode().trim());
        }
        if (StringUtils.isNotBlank(request.deptName())) {
            existing.setDeptName(request.deptName());
        }
        existing.setLeader(request.leader());
        existing.setPhone(request.phone());
        existing.setEmail(request.email());
        existing.setSort(request.sort());
        existing.setStatus(request.status());

        String parentId = adminTreeSupport.normalizeParentId(request.parentId());
        adminTreeSupport.validateNoCycle(existing.getId(), parentId, value -> queryParentId(existing.getTenantId(), value));
        existing.setParentId(parentId);
        existing.setParentIds(adminTreeSupport.resolveParentIds(parentId, value -> queryParentIds(existing.getTenantId(), value)));

        iamDeptMapper.updateById(existing);
        return existing;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String tenantId, String deptId) {
        IamDeptEntity existing = requiredDept(tenantId, deptId);
        Long childCount = iamDeptMapper.selectCount(new LambdaQueryWrapper<IamDeptEntity>()
                .eq(IamDeptEntity::getTenantId, existing.getTenantId())
                .eq(IamDeptEntity::getParentId, existing.getId()));
        if (childCount != null && childCount > 0) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "请先删除子部门");
        }

        iamDeptMapper.deleteById(existing.getId());
        iamUserDeptMapper.delete(new LambdaQueryWrapper<IamUserDeptEntity>()
                .eq(IamUserDeptEntity::getTenantId, existing.getTenantId())
                .eq(IamUserDeptEntity::getDeptId, existing.getId()));
        iamRoleDeptMapper.delete(new LambdaQueryWrapper<IamRoleDeptEntity>()
                .eq(IamRoleDeptEntity::getTenantId, existing.getTenantId())
                .eq(IamRoleDeptEntity::getDeptId, existing.getId()));
        return true;
    }

    private IamDeptEntity requiredDept(String tenantId, String deptId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        IamDeptEntity existing = iamDeptMapper.selectOne(new LambdaQueryWrapper<IamDeptEntity>()
                .eq(IamDeptEntity::getTenantId, resolvedTenantId)
                .eq(IamDeptEntity::getId, deptId));
        if (existing == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "部门不存在");
        }
        return existing;
    }

    private void ensureDeptCodeUnique(String tenantId, String deptCode, String excludeId) {
        LambdaQueryWrapper<IamDeptEntity> wrapper = new LambdaQueryWrapper<IamDeptEntity>()
                .eq(IamDeptEntity::getTenantId, tenantId)
                .eq(IamDeptEntity::getDeptCode, deptCode.trim());
        if (StringUtils.isNotBlank(excludeId)) {
            wrapper.ne(IamDeptEntity::getId, excludeId);
        }
        Long count = iamDeptMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(AdminErrorCode.ADMIN_UNIQUENESS_CONFLICT, "部门编码已存在");
        }
    }

    private String queryParentIds(String tenantId, String parentId) {
        if (StringUtils.isBlank(parentId) || "0".equals(parentId)) {
            return "0";
        }
        IamDeptEntity parent = iamDeptMapper.selectOne(new LambdaQueryWrapper<IamDeptEntity>()
                .eq(IamDeptEntity::getTenantId, tenantId)
                .eq(IamDeptEntity::getId, parentId));
        if (parent == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "父级部门不存在");
        }
        if (StringUtils.isBlank(parent.getParentIds())) {
            return "0," + parent.getId();
        }
        return parent.getParentIds();
    }

    private String queryParentId(String tenantId, String id) {
        if (StringUtils.isBlank(id) || "0".equals(id)) {
            return "0";
        }
        IamDeptEntity parent = iamDeptMapper.selectOne(new LambdaQueryWrapper<IamDeptEntity>()
                .eq(IamDeptEntity::getTenantId, tenantId)
                .eq(IamDeptEntity::getId, id));
        if (parent == null || StringUtils.isBlank(parent.getParentId())) {
            return "0";
        }
        return parent.getParentId();
    }
}
