package com.ysmjjsy.goya.component.mybatisplus.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ysmjjsy.goya.component.framework.security.context.ResourceContext;
import com.ysmjjsy.goya.component.framework.security.context.ResourceResolver;
import com.ysmjjsy.goya.component.framework.security.domain.Resource;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContext;
import com.ysmjjsy.goya.component.mybatisplus.permission.converter.ResourceConverter;
import com.ysmjjsy.goya.component.mybatisplus.permission.entity.DataResourceEntity;
import com.ysmjjsy.goya.component.mybatisplus.permission.mapper.DataResourceMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>基于 data_resource 表的资源解析器。</p>
 *
 * @author goya
 * @since 2026/1/31 11:30
 */
@RequiredArgsConstructor
public class DataResourceResolver implements ResourceResolver {

    private final DataResourceMapper resourceMapper;
    private final ResourceConverter resourceConverter;

    /**
     * 解析资源信息。
     *
     * @param context 资源上下文
     * @return 解析后的资源
     */
    @Override
    public Resource resolve(@NonNull ResourceContext context) {
        if (!StringUtils.hasText(context.getResourceCode())) {
            return null;
        }
        DataResourceEntity entity = loadEntity(context.getResourceCode(), resolveTenantCode(context.getAttributes()));
        if (entity == null) {
            return null;
        }
        return resourceConverter.toTarget(entity);
    }

    /**
     * 解析资源的子级列表。
     *
     * @param resource 资源
     * @return 子级资源列表
     */
    @Override
    public List<Resource> resolveChildren(@NonNull Resource resource) {
        if (!StringUtils.hasText(resource.getResourceCode())) {
            return Collections.emptyList();
        }
        String tenantCode = resolveTenantCode(resource.getAttributes());
        LambdaQueryWrapper<DataResourceEntity> wrapper = Wrappers.lambdaQuery(DataResourceEntity.class);
        wrapper.eq(DataResourceEntity::getResourceParentCode, resource.getResourceCode());
        if (StringUtils.hasText(tenantCode)) {
            wrapper.eq(DataResourceEntity::getTenantCode, tenantCode);
        }
        return resourceMapper.selectList(wrapper)
                .stream()
                .map(resourceConverter::toTarget)
                .collect(Collectors.toList());
    }

    private DataResourceEntity loadEntity(String resourceCode, String tenantCode) {
        LambdaQueryWrapper<DataResourceEntity> wrapper = Wrappers.lambdaQuery(DataResourceEntity.class);
        wrapper.eq(DataResourceEntity::getResourceCode, resourceCode);
        if (StringUtils.hasText(tenantCode)) {
            wrapper.eq(DataResourceEntity::getTenantCode, tenantCode);
        }
        return resourceMapper.selectOne(wrapper);
    }


    private String resolveTenantCode(Map<String, Object> attributes) {
        if (attributes != null) {
            Object tenantCode = attributes.get("tenantCode");
            if (tenantCode instanceof String value && StringUtils.hasText(value)) {
                return value;
            }
        }
        return TenantContext.get().tenantId();
    }
}
