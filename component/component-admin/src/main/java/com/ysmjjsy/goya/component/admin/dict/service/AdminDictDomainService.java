package com.ysmjjsy.goya.component.admin.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ysmjjsy.goya.component.admin.dict.dto.DictItemSaveRequest;
import com.ysmjjsy.goya.component.admin.dict.dto.DictTypeSaveRequest;
import com.ysmjjsy.goya.component.admin.dict.entity.IamDictItemEntity;
import com.ysmjjsy.goya.component.admin.dict.entity.IamDictTypeEntity;
import com.ysmjjsy.goya.component.admin.dict.mapper.IamDictItemMapper;
import com.ysmjjsy.goya.component.admin.dict.mapper.IamDictTypeMapper;
import com.ysmjjsy.goya.component.admin.error.AdminErrorCode;
import com.ysmjjsy.goya.component.admin.support.AdminTenantSupport;
import com.ysmjjsy.goya.component.framework.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>字典管理领域服务</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Service
@RequiredArgsConstructor
public class AdminDictDomainService {

    private final IamDictTypeMapper iamDictTypeMapper;
    private final IamDictItemMapper iamDictItemMapper;
    private final AdminTenantSupport adminTenantSupport;

    public List<IamDictTypeEntity> listTypes(String tenantId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        return iamDictTypeMapper.selectList(new LambdaQueryWrapper<IamDictTypeEntity>()
                .eq(IamDictTypeEntity::getTenantId, resolvedTenantId)
                .orderByAsc(IamDictTypeEntity::getDictTypeCode));
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDictTypeEntity createType(DictTypeSaveRequest request) {
        if (request == null || StringUtils.isBlank(request.dictTypeCode())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "dictTypeCode 不能为空");
        }
        String tenantId = adminTenantSupport.requiredTenantId(request.tenantId());
        ensureTypeCodeUnique(tenantId, request.dictTypeCode(), null);

        IamDictTypeEntity entity = new IamDictTypeEntity();
        entity.setTenantId(tenantId);
        entity.setDictTypeCode(request.dictTypeCode().trim());
        entity.setDictTypeName(request.dictTypeName());
        entity.setStatus(request.status());
        entity.setRemark(request.remark());
        iamDictTypeMapper.insert(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDictTypeEntity updateType(String typeId, DictTypeSaveRequest request) {
        IamDictTypeEntity existing = requiredType(request == null ? null : request.tenantId(), typeId);
        if (request == null) {
            return existing;
        }
        if (StringUtils.isNotBlank(request.dictTypeCode())) {
            ensureTypeCodeUnique(existing.getTenantId(), request.dictTypeCode(), existing.getId());
            existing.setDictTypeCode(request.dictTypeCode().trim());
        }
        existing.setDictTypeName(request.dictTypeName());
        existing.setStatus(request.status());
        existing.setRemark(request.remark());
        iamDictTypeMapper.updateById(existing);
        return existing;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteType(String tenantId, String typeId) {
        IamDictTypeEntity existing = requiredType(tenantId, typeId);
        iamDictTypeMapper.deleteById(existing.getId());
        iamDictItemMapper.delete(new LambdaQueryWrapper<IamDictItemEntity>()
                .eq(IamDictItemEntity::getTenantId, existing.getTenantId())
                .eq(IamDictItemEntity::getDictTypeCode, existing.getDictTypeCode()));
        return true;
    }

    public List<IamDictItemEntity> listItems(String tenantId, String typeCode) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        return iamDictItemMapper.selectList(new LambdaQueryWrapper<IamDictItemEntity>()
                .eq(IamDictItemEntity::getTenantId, resolvedTenantId)
                .eq(IamDictItemEntity::getDictTypeCode, typeCode)
                .orderByAsc(IamDictItemEntity::getSort)
                .orderByAsc(IamDictItemEntity::getItemCode));
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDictItemEntity createItem(DictItemSaveRequest request) {
        if (request == null || StringUtils.isBlank(request.dictTypeCode()) || StringUtils.isBlank(request.itemCode())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "dictTypeCode/itemCode 不能为空");
        }
        String tenantId = adminTenantSupport.requiredTenantId(request.tenantId());
        String dictTypeCode = request.dictTypeCode().trim();
        requiredTypeByCode(tenantId, dictTypeCode);
        ensureItemCodeUnique(tenantId, dictTypeCode, request.itemCode(), null);

        IamDictItemEntity entity = new IamDictItemEntity();
        entity.setTenantId(tenantId);
        entity.setDictTypeCode(dictTypeCode);
        entity.setItemCode(request.itemCode().trim());
        entity.setItemLabel(request.itemLabel());
        entity.setItemValue(request.itemValue());
        entity.setSort(request.sort());
        entity.setStatus(request.status());
        entity.setRemark(request.remark());
        iamDictItemMapper.insert(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDictItemEntity updateItem(String itemId, DictItemSaveRequest request) {
        IamDictItemEntity existing = requiredItem(request == null ? null : request.tenantId(), itemId);
        if (request == null) {
            return existing;
        }
        String dictTypeCode = StringUtils.defaultIfBlank(request.dictTypeCode(), existing.getDictTypeCode()).trim();
        requiredTypeByCode(existing.getTenantId(), dictTypeCode);
        if (StringUtils.isNotBlank(request.itemCode())) {
            ensureItemCodeUnique(existing.getTenantId(), dictTypeCode, request.itemCode(), existing.getId());
            existing.setItemCode(request.itemCode().trim());
        }
        existing.setDictTypeCode(dictTypeCode);
        existing.setItemLabel(request.itemLabel());
        existing.setItemValue(request.itemValue());
        existing.setSort(request.sort());
        existing.setStatus(request.status());
        existing.setRemark(request.remark());
        iamDictItemMapper.updateById(existing);
        return existing;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteItem(String tenantId, String itemId) {
        IamDictItemEntity existing = requiredItem(tenantId, itemId);
        iamDictItemMapper.deleteById(existing.getId());
        return true;
    }

    private IamDictTypeEntity requiredType(String tenantId, String typeId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        IamDictTypeEntity existing = iamDictTypeMapper.selectOne(new LambdaQueryWrapper<IamDictTypeEntity>()
                .eq(IamDictTypeEntity::getTenantId, resolvedTenantId)
                .eq(IamDictTypeEntity::getId, typeId));
        if (existing == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "字典类型不存在");
        }
        return existing;
    }

    private IamDictItemEntity requiredItem(String tenantId, String itemId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        IamDictItemEntity existing = iamDictItemMapper.selectOne(new LambdaQueryWrapper<IamDictItemEntity>()
                .eq(IamDictItemEntity::getTenantId, resolvedTenantId)
                .eq(IamDictItemEntity::getId, itemId));
        if (existing == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "字典条目不存在");
        }
        return existing;
    }

    private IamDictTypeEntity requiredTypeByCode(String tenantId, String typeCode) {
        String normalizedTypeCode = StringUtils.trimToEmpty(typeCode);
        IamDictTypeEntity existing = iamDictTypeMapper.selectOne(new LambdaQueryWrapper<IamDictTypeEntity>()
                .eq(IamDictTypeEntity::getTenantId, tenantId)
                .eq(IamDictTypeEntity::getDictTypeCode, normalizedTypeCode));
        if (existing == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "字典类型不存在");
        }
        return existing;
    }

    private void ensureTypeCodeUnique(String tenantId, String typeCode, String excludeId) {
        LambdaQueryWrapper<IamDictTypeEntity> wrapper = new LambdaQueryWrapper<IamDictTypeEntity>()
                .eq(IamDictTypeEntity::getTenantId, tenantId)
                .eq(IamDictTypeEntity::getDictTypeCode, typeCode.trim());
        if (StringUtils.isNotBlank(excludeId)) {
            wrapper.ne(IamDictTypeEntity::getId, excludeId);
        }
        Long count = iamDictTypeMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(AdminErrorCode.ADMIN_UNIQUENESS_CONFLICT, "字典类型编码已存在");
        }
    }

    private void ensureItemCodeUnique(String tenantId, String typeCode, String itemCode, String excludeId) {
        LambdaQueryWrapper<IamDictItemEntity> wrapper = new LambdaQueryWrapper<IamDictItemEntity>()
                .eq(IamDictItemEntity::getTenantId, tenantId)
                .eq(IamDictItemEntity::getDictTypeCode, typeCode)
                .eq(IamDictItemEntity::getItemCode, itemCode.trim());
        if (StringUtils.isNotBlank(excludeId)) {
            wrapper.ne(IamDictItemEntity::getId, excludeId);
        }
        Long count = iamDictItemMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(AdminErrorCode.ADMIN_UNIQUENESS_CONFLICT, "字典条目编码已存在");
        }
    }
}
