package com.ysmjjsy.goya.component.admin.menu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ysmjjsy.goya.component.admin.error.AdminErrorCode;
import com.ysmjjsy.goya.component.admin.menu.dto.MenuSaveRequest;
import com.ysmjjsy.goya.component.admin.menu.entity.IamMenuEntity;
import com.ysmjjsy.goya.component.admin.menu.mapper.IamMenuMapper;
import com.ysmjjsy.goya.component.admin.role.entity.IamRoleMenuEntity;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRoleMenuMapper;
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
 * <p>菜单管理领域服务</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Service
@RequiredArgsConstructor
public class AdminMenuDomainService {

    private final IamMenuMapper iamMenuMapper;
    private final IamRoleMenuMapper iamRoleMenuMapper;
    private final AdminTenantSupport adminTenantSupport;
    private final AdminTreeSupport adminTreeSupport;

    public List<IamMenuEntity> tree(String tenantId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        List<IamMenuEntity> list = iamMenuMapper.selectList(new LambdaQueryWrapper<IamMenuEntity>()
                .eq(IamMenuEntity::getTenantId, resolvedTenantId)
                .orderByAsc(IamMenuEntity::getSort)
                .orderByAsc(IamMenuEntity::getMenuCode));
        list.sort(Comparator.comparing(item -> item.getSort() == null ? Integer.MAX_VALUE : item.getSort()));
        return adminTreeSupport.buildTree(list);
    }

    public IamMenuEntity detail(String tenantId, String menuId) {
        return requiredMenu(tenantId, menuId);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamMenuEntity create(MenuSaveRequest request) {
        if (request == null || StringUtils.isBlank(request.menuCode()) || StringUtils.isBlank(request.menuName())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "menuCode/menuName 不能为空");
        }
        String tenantId = adminTenantSupport.requiredTenantId(request.tenantId());
        ensureMenuCodeUnique(tenantId, request.menuCode(), null);

        IamMenuEntity entity = new IamMenuEntity();
        entity.setTenantId(tenantId);
        entity.setMenuCode(request.menuCode().trim());
        entity.setMenuName(request.menuName());
        entity.setMenuType(request.menuType());
        entity.setPath(request.path());
        entity.setComponent(request.component());
        entity.setPermissionCode(request.permissionCode());
        entity.setIcon(request.icon());
        entity.setSort(request.sort());
        entity.setStatus(request.status());
        entity.setHidden(request.hidden());

        String parentId = adminTreeSupport.normalizeParentId(request.parentId());
        entity.setParentId(parentId);
        entity.setParentIds(adminTreeSupport.resolveParentIds(parentId, value -> queryParentIds(tenantId, value)));
        iamMenuMapper.insert(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public IamMenuEntity update(String menuId, MenuSaveRequest request) {
        IamMenuEntity existing = requiredMenu(request == null ? null : request.tenantId(), menuId);
        if (request == null) {
            return existing;
        }

        if (StringUtils.isNotBlank(request.menuCode())) {
            ensureMenuCodeUnique(existing.getTenantId(), request.menuCode(), existing.getId());
            existing.setMenuCode(request.menuCode().trim());
        }
        if (StringUtils.isNotBlank(request.menuName())) {
            existing.setMenuName(request.menuName());
        }
        existing.setMenuType(request.menuType());
        existing.setPath(request.path());
        existing.setComponent(request.component());
        existing.setPermissionCode(request.permissionCode());
        existing.setIcon(request.icon());
        existing.setSort(request.sort());
        existing.setStatus(request.status());
        existing.setHidden(request.hidden());

        String parentId = adminTreeSupport.normalizeParentId(request.parentId());
        adminTreeSupport.validateNoCycle(existing.getId(), parentId, value -> queryParentId(existing.getTenantId(), value));
        existing.setParentId(parentId);
        existing.setParentIds(adminTreeSupport.resolveParentIds(parentId, value -> queryParentIds(existing.getTenantId(), value)));

        iamMenuMapper.updateById(existing);
        return existing;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String tenantId, String menuId) {
        IamMenuEntity existing = requiredMenu(tenantId, menuId);
        Long childCount = iamMenuMapper.selectCount(new LambdaQueryWrapper<IamMenuEntity>()
                .eq(IamMenuEntity::getTenantId, existing.getTenantId())
                .eq(IamMenuEntity::getParentId, existing.getId()));
        if (childCount != null && childCount > 0) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "请先删除子菜单");
        }

        iamMenuMapper.deleteById(existing.getId());
        iamRoleMenuMapper.delete(new LambdaQueryWrapper<IamRoleMenuEntity>()
                .eq(IamRoleMenuEntity::getTenantId, existing.getTenantId())
                .eq(IamRoleMenuEntity::getMenuId, existing.getId()));
        return true;
    }

    private IamMenuEntity requiredMenu(String tenantId, String menuId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        IamMenuEntity existing = iamMenuMapper.selectOne(new LambdaQueryWrapper<IamMenuEntity>()
                .eq(IamMenuEntity::getTenantId, resolvedTenantId)
                .eq(IamMenuEntity::getId, menuId));
        if (existing == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "菜单不存在");
        }
        return existing;
    }

    private void ensureMenuCodeUnique(String tenantId, String menuCode, String excludeId) {
        LambdaQueryWrapper<IamMenuEntity> wrapper = new LambdaQueryWrapper<IamMenuEntity>()
                .eq(IamMenuEntity::getTenantId, tenantId)
                .eq(IamMenuEntity::getMenuCode, menuCode.trim());
        if (StringUtils.isNotBlank(excludeId)) {
            wrapper.ne(IamMenuEntity::getId, excludeId);
        }
        Long count = iamMenuMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(AdminErrorCode.ADMIN_UNIQUENESS_CONFLICT, "菜单编码已存在");
        }
    }

    private String queryParentIds(String tenantId, String parentId) {
        if (StringUtils.isBlank(parentId) || "0".equals(parentId)) {
            return "0";
        }
        IamMenuEntity parent = iamMenuMapper.selectOne(new LambdaQueryWrapper<IamMenuEntity>()
                .eq(IamMenuEntity::getTenantId, tenantId)
                .eq(IamMenuEntity::getId, parentId));
        if (parent == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "父级菜单不存在");
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
        IamMenuEntity parent = iamMenuMapper.selectOne(new LambdaQueryWrapper<IamMenuEntity>()
                .eq(IamMenuEntity::getTenantId, tenantId)
                .eq(IamMenuEntity::getId, id));
        if (parent == null || StringUtils.isBlank(parent.getParentId())) {
            return "0";
        }
        return parent.getParentId();
    }
}
