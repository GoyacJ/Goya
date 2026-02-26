package com.ysmjjsy.goya.component.admin.policy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ysmjjsy.goya.component.admin.configuration.properties.AdminProperties;
import com.ysmjjsy.goya.component.admin.constants.AdminConst;
import com.ysmjjsy.goya.component.admin.error.AdminErrorCode;
import com.ysmjjsy.goya.component.admin.permission.entity.IamPermissionEntity;
import com.ysmjjsy.goya.component.admin.permission.mapper.IamPermissionMapper;
import com.ysmjjsy.goya.component.admin.policy.dto.PolicySaveRequest;
import com.ysmjjsy.goya.component.admin.policy.dto.ResourceSaveRequest;
import com.ysmjjsy.goya.component.admin.role.entity.IamRolePermissionEntity;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRolePermissionMapper;
import com.ysmjjsy.goya.component.admin.support.AdminTenantSupport;
import com.ysmjjsy.goya.component.framework.common.exception.BizException;
import com.ysmjjsy.goya.component.framework.security.domain.PolicyEffect;
import com.ysmjjsy.goya.component.framework.security.domain.PolicyScope;
import com.ysmjjsy.goya.component.framework.security.domain.ResourceRange;
import com.ysmjjsy.goya.component.framework.security.domain.ResourceType;
import com.ysmjjsy.goya.component.framework.security.domain.SubjectType;
import com.ysmjjsy.goya.component.framework.security.event.PermissionChangeEvent;
import com.ysmjjsy.goya.component.framework.security.event.PermissionChangeType;
import com.ysmjjsy.goya.component.framework.security.spi.PermissionChangePublisher;
import com.ysmjjsy.goya.component.mybatisplus.permission.entity.DataResourceEntity;
import com.ysmjjsy.goya.component.mybatisplus.permission.entity.DataResourcePolicyEntity;
import com.ysmjjsy.goya.component.mybatisplus.permission.mapper.DataResourceMapper;
import com.ysmjjsy.goya.component.mybatisplus.permission.mapper.DataResourcePolicyMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>策略与资源领域服务</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Service
@RequiredArgsConstructor
public class AdminPolicyDomainService {

    private final DataResourceMapper dataResourceMapper;
    private final DataResourcePolicyMapper dataResourcePolicyMapper;
    private final IamRolePermissionMapper iamRolePermissionMapper;
    private final IamPermissionMapper iamPermissionMapper;
    private final PermissionChangePublisher permissionChangePublisher;
    private final AdminTenantSupport adminTenantSupport;
    private final AdminProperties adminProperties;

    public List<DataResourcePolicyEntity> listPolicies(String tenantId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        return dataResourcePolicyMapper.selectList(new LambdaQueryWrapper<DataResourcePolicyEntity>()
                .eq(DataResourcePolicyEntity::getTenantId, resolvedTenantId)
                .orderByDesc(DataResourcePolicyEntity::getUpdatedAt));
    }

    @Transactional(rollbackFor = Exception.class)
    public DataResourcePolicyEntity createPolicy(PolicySaveRequest request) {
        validatePolicyRequest(request);
        DataResourcePolicyEntity entity = toPolicyEntity(new DataResourcePolicyEntity(), request);
        dataResourcePolicyMapper.insert(entity);
        publishPolicyEvent(entity, PermissionChangeType.CREATE);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public DataResourcePolicyEntity updatePolicy(String policyId, PolicySaveRequest request) {
        validatePolicyRequest(request);
        String tenantId = adminTenantSupport.requiredTenantId(request == null ? null : request.tenantId());
        DataResourcePolicyEntity existing = requiredPolicy(tenantId, policyId);
        DataResourcePolicyEntity entity = toPolicyEntity(existing, request);
        dataResourcePolicyMapper.updateById(entity);
        publishPolicyEvent(entity, PermissionChangeType.UPDATE);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePolicy(String tenantId, String policyId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        DataResourcePolicyEntity existing = dataResourcePolicyMapper.selectOne(new LambdaQueryWrapper<DataResourcePolicyEntity>()
                .eq(DataResourcePolicyEntity::getTenantId, resolvedTenantId)
                .eq(DataResourcePolicyEntity::getId, policyId));
        if (existing == null) {
            return true;
        }
        dataResourcePolicyMapper.deleteById(existing.getId());
        publishPolicyEvent(existing, PermissionChangeType.DELETE);
        return true;
    }

    public List<DataResourceEntity> listResources(String tenantId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        return dataResourceMapper.selectList(new LambdaQueryWrapper<DataResourceEntity>()
                .eq(DataResourceEntity::getTenantId, resolvedTenantId)
                .orderByAsc(DataResourceEntity::getResourceCode));
    }

    @Transactional(rollbackFor = Exception.class)
    public DataResourceEntity createResource(ResourceSaveRequest request) {
        validateResourceRequest(request);
        String tenantId = adminTenantSupport.requiredTenantId(request == null ? null : request.tenantId());
        DataResourceEntity entity = new DataResourceEntity();
        entity.setTenantId(tenantId);
        entity.setTenantCode(tenantId);
        if (request != null) {
            entity.setResourceHashcode(StringUtils.defaultIfBlank(request.resourceHashcode(), request.resourceCode()));
            entity.setResourceCode(request.resourceCode());
            entity.setResourceParentCode(request.resourceParentCode());
            entity.setResourceParentCodes(request.resourceParentCodes());
            entity.setResourceOperType(request.resourceOperType());
            entity.setResourceName(request.resourceName());
            entity.setResourceType(parseResourceType(request.resourceType()));
            entity.setResourceDesc(request.resourceDesc());
            entity.setResourceOwner(request.resourceOwner());
        }
        dataResourceMapper.insert(entity);
        publishResourceEvent(entity, PermissionChangeType.CREATE);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public DataResourceEntity updateResource(String resourceId, ResourceSaveRequest request) {
        validateResourceRequest(request);
        String tenantId = adminTenantSupport.requiredTenantId(request == null ? null : request.tenantId());
        DataResourceEntity existing = requiredResource(tenantId, resourceId);
        if (request != null) {
            existing.setTenantId(tenantId);
            existing.setTenantCode(tenantId);
            existing.setResourceHashcode(StringUtils.defaultIfBlank(request.resourceHashcode(), StringUtils.defaultIfBlank(existing.getResourceHashcode(), request.resourceCode())));
            existing.setResourceCode(StringUtils.defaultIfBlank(request.resourceCode(), existing.getResourceCode()));
            existing.setResourceParentCode(request.resourceParentCode());
            existing.setResourceParentCodes(request.resourceParentCodes());
            existing.setResourceOperType(request.resourceOperType());
            existing.setResourceName(request.resourceName());
            existing.setResourceType(parseResourceType(request.resourceType()));
            existing.setResourceDesc(request.resourceDesc());
            existing.setResourceOwner(request.resourceOwner());
        }
        dataResourceMapper.updateById(existing);
        publishResourceEvent(existing, PermissionChangeType.UPDATE);
        return existing;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteResource(String tenantId, String resourceId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        DataResourceEntity existing = dataResourceMapper.selectOne(new LambdaQueryWrapper<DataResourceEntity>()
                .eq(DataResourceEntity::getTenantId, resolvedTenantId)
                .eq(DataResourceEntity::getId, resourceId));
        if (existing == null) {
            return true;
        }
        dataResourceMapper.deleteById(existing.getId());
        publishResourceEvent(existing, PermissionChangeType.DELETE);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncRolePermissionPolicies(String tenantId, String roleId, String roleCode) {
        if (!adminProperties.policy().syncEnabled() || StringUtils.isAnyBlank(tenantId, roleId, roleCode)) {
            return;
        }

        List<IamRolePermissionEntity> rolePermissions = iamRolePermissionMapper.selectList(new LambdaQueryWrapper<IamRolePermissionEntity>()
                .eq(IamRolePermissionEntity::getTenantId, tenantId)
                .eq(IamRolePermissionEntity::getRoleId, roleId));
        if (rolePermissions.isEmpty()) {
            removeRolePolicies(tenantId, roleCode, Collections.emptySet());
            return;
        }

        List<String> permissionIds = rolePermissions.stream()
                .map(IamRolePermissionEntity::getPermissionId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();

        List<IamPermissionEntity> permissions = permissionIds.isEmpty()
                ? List.of()
                : iamPermissionMapper.selectList(new LambdaQueryWrapper<IamPermissionEntity>()
                .eq(IamPermissionEntity::getTenantId, tenantId)
                .in(IamPermissionEntity::getId, permissionIds));

        Map<String, DataResourcePolicyEntity> existedByKey = loadRolePolicyMap(tenantId, roleCode);
        Set<String> desiredKeys = new LinkedHashSet<>();

        for (IamPermissionEntity permission : permissions) {
            if (permission == null || StringUtils.isBlank(permission.getResourceCode())) {
                continue;
            }
            String action = StringUtils.defaultIfBlank(permission.getActionCode(), AdminConst.POLICY_ACTION_ACCESS);
            String key = policyKey(permission.getResourceCode(), action);
            desiredKeys.add(key);

            DataResourcePolicyEntity policyEntity = existedByKey.get(key);
            if (policyEntity == null) {
                policyEntity = new DataResourcePolicyEntity();
                policyEntity.setTenantId(tenantId);
                policyEntity.setTenantCode(tenantId);
                policyEntity.setSubjectType(SubjectType.ROLE);
                policyEntity.setSubjectId(roleCode);
                policyEntity.setResourceType(ResourceType.API);
                policyEntity.setResourceCode(permission.getResourceCode());
                policyEntity.setActionCode(action);
                policyEntity.setPolicyEffect(PolicyEffect.ALLOW);
                policyEntity.setPolicyScope(PolicyScope.RESOURCE);
                policyEntity.setInheritFlag(true);
                policyEntity.setResourceRange(ResourceRange.SELF_AND_CHILDREN);
                policyEntity.setNeverExpire(true);
                dataResourcePolicyMapper.insert(policyEntity);
                publishPolicyEvent(policyEntity, PermissionChangeType.CREATE);
            } else {
                policyEntity.setPolicyEffect(PolicyEffect.ALLOW);
                policyEntity.setPolicyScope(PolicyScope.RESOURCE);
                policyEntity.setResourceType(ResourceType.API);
                policyEntity.setResourceCode(permission.getResourceCode());
                policyEntity.setActionCode(action);
                policyEntity.setInheritFlag(true);
                policyEntity.setResourceRange(ResourceRange.SELF_AND_CHILDREN);
                policyEntity.setNeverExpire(true);
                policyEntity.setExpireTime(null);
                dataResourcePolicyMapper.updateById(policyEntity);
                publishPolicyEvent(policyEntity, PermissionChangeType.UPDATE);
            }
        }

        removeRolePolicies(tenantId, roleCode, desiredKeys);
    }

    @Transactional(rollbackFor = Exception.class)
    public void bootstrapSuperAdminPolicies(String tenantId, String roleId, String roleCode) {
        if (StringUtils.isAnyBlank(tenantId, roleId, roleCode)) {
            return;
        }
        syncRolePermissionPolicies(tenantId, roleId, roleCode);

        List<DataResourceEntity> apiResources = dataResourceMapper.selectList(new LambdaQueryWrapper<DataResourceEntity>()
                .eq(DataResourceEntity::getTenantId, tenantId)
                .eq(DataResourceEntity::getResourceType, ResourceType.API));
        for (DataResourceEntity apiResource : apiResources) {
            if (apiResource == null || StringUtils.isBlank(apiResource.getResourceCode())) {
                continue;
            }
            String key = policyKey(apiResource.getResourceCode(), AdminConst.POLICY_ACTION_ACCESS);
            Map<String, DataResourcePolicyEntity> existedByKey = loadRolePolicyMap(tenantId, roleCode);
            if (existedByKey.containsKey(key)) {
                continue;
            }
            DataResourcePolicyEntity policyEntity = new DataResourcePolicyEntity();
            policyEntity.setTenantId(tenantId);
            policyEntity.setTenantCode(tenantId);
            policyEntity.setSubjectType(SubjectType.ROLE);
            policyEntity.setSubjectId(roleCode);
            policyEntity.setResourceType(ResourceType.API);
            policyEntity.setResourceCode(apiResource.getResourceCode());
            policyEntity.setActionCode(AdminConst.POLICY_ACTION_ACCESS);
            policyEntity.setPolicyEffect(PolicyEffect.ALLOW);
            policyEntity.setPolicyScope(PolicyScope.RESOURCE);
            policyEntity.setInheritFlag(true);
            policyEntity.setResourceRange(ResourceRange.SELF_AND_CHILDREN);
            policyEntity.setNeverExpire(true);
            dataResourcePolicyMapper.insert(policyEntity);
            publishPolicyEvent(policyEntity, PermissionChangeType.PUBLISH);
        }
    }

    private DataResourcePolicyEntity toPolicyEntity(DataResourcePolicyEntity target, PolicySaveRequest request) {
        String tenantId = adminTenantSupport.requiredTenantId(request == null ? null : request.tenantId());
        target.setTenantId(tenantId);
        target.setTenantCode(tenantId);
        if (request == null) {
            return target;
        }
        target.setSubjectType(parseSubjectType(request.subjectType()));
        target.setSubjectId(request.subjectId());
        target.setResourceType(parseResourceType(request.resourceType()));
        target.setResourceCode(request.resourceCode());
        target.setActionCode(request.actionCode());
        target.setPolicyEffect(parsePolicyEffect(request.policyEffect()));
        target.setPolicyScope(parsePolicyScope(request.policyScope()));
        target.setRangeDsl(request.rangeDsl());
        target.setAllowColumns(joinCsv(request.allowColumns()));
        target.setDenyColumns(joinCsv(request.denyColumns()));
        target.setInheritFlag(Boolean.TRUE.equals(request.inheritFlag()));
        target.setResourceRange(parseResourceRange(request.resourceRange()));
        target.setNeverExpire(Boolean.TRUE.equals(request.neverExpire()));
        target.setExpireTime(request.expireTime());
        return target;
    }

    private void validatePolicyRequest(PolicySaveRequest request) {
        if (request == null
                || StringUtils.isAnyBlank(request.subjectId(), request.resourceCode(), request.actionCode())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "subjectId/resourceCode/actionCode 不能为空");
        }
    }

    private void validateResourceRequest(ResourceSaveRequest request) {
        if (request == null || StringUtils.isBlank(request.resourceCode())) {
            throw new BizException(AdminErrorCode.ADMIN_PARAM_INVALID, "resourceCode 不能为空");
        }
    }

    private DataResourcePolicyEntity requiredPolicy(String tenantId, String policyId) {
        DataResourcePolicyEntity existing = dataResourcePolicyMapper.selectOne(new LambdaQueryWrapper<DataResourcePolicyEntity>()
                .eq(DataResourcePolicyEntity::getTenantId, tenantId)
                .eq(DataResourcePolicyEntity::getId, policyId));
        if (existing == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "策略不存在");
        }
        return existing;
    }

    private DataResourceEntity requiredResource(String tenantId, String resourceId) {
        DataResourceEntity existing = dataResourceMapper.selectOne(new LambdaQueryWrapper<DataResourceEntity>()
                .eq(DataResourceEntity::getTenantId, tenantId)
                .eq(DataResourceEntity::getId, resourceId));
        if (existing == null) {
            throw new BizException(AdminErrorCode.ADMIN_ENTITY_NOT_FOUND, "资源不存在");
        }
        return existing;
    }

    private Map<String, DataResourcePolicyEntity> loadRolePolicyMap(String tenantId, String roleCode) {
        List<DataResourcePolicyEntity> entities = dataResourcePolicyMapper.selectList(new LambdaQueryWrapper<DataResourcePolicyEntity>()
                .eq(DataResourcePolicyEntity::getTenantId, tenantId)
                .eq(DataResourcePolicyEntity::getSubjectType, SubjectType.ROLE)
                .eq(DataResourcePolicyEntity::getSubjectId, roleCode)
                .eq(DataResourcePolicyEntity::getResourceType, ResourceType.API));
        Map<String, DataResourcePolicyEntity> mapping = new LinkedHashMap<>();
        for (DataResourcePolicyEntity entity : entities) {
            String key = policyKey(entity.getResourceCode(), StringUtils.defaultIfBlank(entity.getActionCode(), AdminConst.POLICY_ACTION_ACCESS));
            mapping.put(key, entity);
        }
        return mapping;
    }

    private void removeRolePolicies(String tenantId, String roleCode, Set<String> desiredKeys) {
        List<DataResourcePolicyEntity> existed = dataResourcePolicyMapper.selectList(new LambdaQueryWrapper<DataResourcePolicyEntity>()
                .eq(DataResourcePolicyEntity::getTenantId, tenantId)
                .eq(DataResourcePolicyEntity::getSubjectType, SubjectType.ROLE)
                .eq(DataResourcePolicyEntity::getSubjectId, roleCode)
                .eq(DataResourcePolicyEntity::getResourceType, ResourceType.API));
        for (DataResourcePolicyEntity existing : existed) {
            String key = policyKey(existing.getResourceCode(), StringUtils.defaultIfBlank(existing.getActionCode(), AdminConst.POLICY_ACTION_ACCESS));
            if (desiredKeys.contains(key)) {
                continue;
            }
            dataResourcePolicyMapper.deleteById(existing.getId());
            publishPolicyEvent(existing, PermissionChangeType.DELETE);
        }
    }

    private String policyKey(String resourceCode, String actionCode) {
        return StringUtils.defaultString(resourceCode) + "::" + StringUtils.defaultString(actionCode);
    }

    private SubjectType parseSubjectType(String value) {
        if (StringUtils.isBlank(value)) {
            return SubjectType.USER;
        }
        try {
            return SubjectType.valueOf(value.trim().toUpperCase());
        } catch (Exception ignored) {
            return SubjectType.USER;
        }
    }

    private ResourceType parseResourceType(String value) {
        if (StringUtils.isBlank(value)) {
            return ResourceType.CUSTOM;
        }
        try {
            return ResourceType.valueOf(value.trim().toUpperCase());
        } catch (Exception ignored) {
            return ResourceType.CUSTOM;
        }
    }

    private PolicyEffect parsePolicyEffect(String value) {
        if (StringUtils.isBlank(value)) {
            return PolicyEffect.ALLOW;
        }
        try {
            return PolicyEffect.valueOf(value.trim().toUpperCase());
        } catch (Exception ignored) {
            return PolicyEffect.ALLOW;
        }
    }

    private PolicyScope parsePolicyScope(String value) {
        if (StringUtils.isBlank(value)) {
            return PolicyScope.RESOURCE;
        }
        try {
            return PolicyScope.valueOf(value.trim().toUpperCase());
        } catch (Exception ignored) {
            return PolicyScope.RESOURCE;
        }
    }

    private ResourceRange parseResourceRange(String value) {
        if (StringUtils.isBlank(value)) {
            return ResourceRange.SELF_AND_CHILDREN;
        }
        try {
            return ResourceRange.valueOf(value.trim().toUpperCase());
        } catch (Exception ignored) {
            return ResourceRange.SELF_AND_CHILDREN;
        }
    }

    private String joinCsv(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(","));
    }

    private void publishPolicyEvent(DataResourcePolicyEntity policy, PermissionChangeType type) {
        if (policy == null) {
            return;
        }
        PermissionChangeEvent event = new PermissionChangeEvent();
        event.setTenantCode(policy.getTenantCode());
        event.setPolicyId(policy.getId());
        event.setResourceCode(policy.getResourceCode());
        event.setChangeType(type);
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("subjectType", policy.getSubjectType() == null ? null : policy.getSubjectType().name());
        attributes.put("subjectId", policy.getSubjectId());
        attributes.put("action", policy.getActionCode());
        event.setAttributes(attributes);
        permissionChangePublisher.publish(event);
    }

    private void publishResourceEvent(DataResourceEntity resource, PermissionChangeType type) {
        if (resource == null) {
            return;
        }
        PermissionChangeEvent event = new PermissionChangeEvent();
        event.setTenantCode(resource.getTenantCode());
        event.setPolicyId(resource.getId());
        event.setResourceCode(resource.getResourceCode());
        event.setChangeType(type);
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("resourceType", resource.getResourceType() == null ? null : resource.getResourceType().name());
        event.setAttributes(attributes);
        permissionChangePublisher.publish(event);
    }
}
