package com.ysmjjsy.goya.component.mybatisplus.permission.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaMD5Utils;
import com.ysmjjsy.goya.component.framework.security.domain.ResourceType;
import com.ysmjjsy.goya.component.framework.servlet.scan.IRestMappingHandler;
import com.ysmjjsy.goya.component.framework.servlet.scan.RestMapping;
import com.ysmjjsy.goya.component.mybatisplus.permission.entity.DataResourceEntity;
import com.ysmjjsy.goya.component.mybatisplus.permission.mapper.DataResourceMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>RequestMapping 扫描结果同步到 data_resource。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class DataResourceRestMappingHandler implements IRestMappingHandler {

    private final DataResourceMapper dataResourceMapper;

    public DataResourceRestMappingHandler(DataResourceMapper dataResourceMapper) {
        this.dataResourceMapper = dataResourceMapper;
    }

    @Override
    public void handler(String currentServiceId, List<RestMapping> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            return;
        }

        List<RestMapping> validResources = resources.stream()
                .filter(this::isValidResource)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(validResources)) {
            return;
        }

        for (RestMapping restMapping : validResources) {
            upsertApiResource(currentServiceId, restMapping);
        }
    }

    private boolean isValidResource(RestMapping restMapping) {
        if (restMapping == null) {
            return false;
        }
        if (Boolean.TRUE.equals(restMapping.getElementIgnore())) {
            return false;
        }
        return StringUtils.isNotBlank(restMapping.getMappingCode());
    }

    private void upsertApiResource(String currentServiceId, RestMapping restMapping) {
        String tenantCode = DefaultConst.DEFAULT_TENANT_ID;
        String resourceCode = restMapping.getMappingCode();

        LambdaQueryWrapper<DataResourceEntity> queryWrapper = Wrappers.lambdaQuery(DataResourceEntity.class)
                .eq(DataResourceEntity::getTenantCode, tenantCode)
                .eq(DataResourceEntity::getResourceType, ResourceType.API)
                .eq(DataResourceEntity::getResourceCode, resourceCode);

        DataResourceEntity existingEntity = dataResourceMapper.selectOne(queryWrapper);
        if (existingEntity == null) {
            DataResourceEntity newEntity = new DataResourceEntity();
            applyValues(newEntity, currentServiceId, tenantCode, restMapping);
            dataResourceMapper.insert(newEntity);
            return;
        }

        applyValues(existingEntity, currentServiceId, tenantCode, restMapping);
        dataResourceMapper.updateById(existingEntity);
    }

    private void applyValues(DataResourceEntity target,
                             String currentServiceId,
                             String tenantCode,
                             RestMapping restMapping) {
        target.setTenantId(tenantCode);
        target.setTenantCode(tenantCode);
        target.setResourceType(ResourceType.API);
        target.setResourceCode(restMapping.getMappingCode());
        target.setResourceName(StringUtils.defaultIfBlank(restMapping.getSummary(), restMapping.getMethodName()));
        target.setResourceDesc(restMapping.getDescription());
        target.setResourceOwner(currentServiceId);
        target.setResourceOperType(resolveRequestMethod(restMapping.getRequestMethod()));
        target.setResourceHashcode(buildResourceHashCode(tenantCode, restMapping.getMappingCode()));
    }

    private String buildResourceHashCode(String tenantCode, String mappingCode) {
        return GoyaMD5Utils.md5(tenantCode + ":" + ResourceType.API.getCode() + ":" + mappingCode);
    }

    private String resolveRequestMethod(Set<?> requestMethods) {
        if (CollectionUtils.isEmpty(requestMethods)) {
            return null;
        }
        return requestMethods.iterator().next().toString();
    }
}
