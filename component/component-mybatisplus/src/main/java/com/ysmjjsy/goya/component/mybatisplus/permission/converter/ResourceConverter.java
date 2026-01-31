package com.ysmjjsy.goya.component.mybatisplus.permission.converter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ysmjjsy.goya.component.framework.core.mapstruct.MapStructConfig;
import com.ysmjjsy.goya.component.framework.core.mapstruct.MapStructConverter;
import com.ysmjjsy.goya.component.framework.security.domain.Resource;
import com.ysmjjsy.goya.component.mybatisplus.permission.entity.DataResourceEntity;
import com.ysmjjsy.goya.component.mybatisplus.permission.mapper.DataResourceMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/31 23:16
 */
@Mapper(config = MapStructConfig.class)
public abstract class ResourceConverter implements MapStructConverter<DataResourceEntity, Resource> {

    /**
     * 你要把 allowedColumns 查库塞进 attributes，就必须能访问 resourceMapper。
     * MapStruct 支持在 abstract mapper 里注入 Spring Bean。
     */
    @Autowired
    protected DataResourceMapper resourceMapper;

    // ---------------------------
    // Entity -> Domain
    // ---------------------------

    @Override
    @Mapping(target = "parentCode", source = "resourceParentCode")
    @Mapping(target = "parentCodes", source = "resourceParentCodes", qualifiedByName = "csvToSet")
    @Mapping(target = "attributes", ignore = true)
    public abstract Resource toTarget(DataResourceEntity origin);

    // ---------------------------
    // Domain -> Entity
    // ---------------------------

    @Override
    @InheritInverseConfiguration(name = "toTarget")
    @Mapping(target = "resourceParentCodes", source = "parentCodes", qualifiedByName = "setToCsv")
    // attributes 不落库（如果你需要落库，请明确字段承载）
    @Mapping(target = "resourceHashcode", ignore = true)
    @Mapping(target = "resourceOperType", ignore = true)
    @Mapping(target = "tenantCode", ignore = true)
    public abstract DataResourceEntity toOrigin(Resource target);

    // ---------------------------
    // Lists
    // ---------------------------

    @Override
    public abstract List<Resource> toTargetList(List<DataResourceEntity> originList);

    @Override
    public abstract List<DataResourceEntity> toOriginList(List<Resource> targetList);

    // ---------------------------
    // Incremental update
    // ---------------------------

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "resourceParentCode", source = "parentCode")
    @Mapping(target = "resourceParentCodes", source = "parentCodes", qualifiedByName = "setToCsv")
    // 同样：attributes 不落库
    @Mapping(target = "resourceHashcode", ignore = true)
    @Mapping(target = "resourceOperType", ignore = true)
    public abstract void updateEntity(Resource target, @MappingTarget DataResourceEntity origin);

    // ---------------------------
    // AfterMapping: attributes + allowedColumns
    // ---------------------------

    @AfterMapping
    protected void fillAttributes(DataResourceEntity origin, @MappingTarget Resource target) {
        // 先把已有 attributes 安全拷贝为可变 map
        Map<String, Object> attrs = target.getAttributes() == null
                ? new HashMap<>()
                : new HashMap<>(target.getAttributes());

        // 追加 allowedColumns
        List<String> columns = loadAllowedColumns(origin.getResourceCode(), resolveTenantCode(attrs, origin));
        attrs.put("allowedColumns", columns);

        target.setAttributes(attrs);
    }

    /**
     * parentCodes: csv -> Set
     */
    @Named("csvToSet")
    protected Set<String> csvToSet(String csv) {
        if (!StringUtils.hasText(csv)) {
            return Collections.emptySet();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * parentCodes: Set -> csv
     */
    @Named("setToCsv")
    protected String setToCsv(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        return set.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.joining(","));
    }

    /**
     * tenantCode 优先级：
     * 1) attributes 里显式携带 tenantCode
     * 2) origin.tenantCode
     */
    protected String resolveTenantCode(Map<String, Object> attrs, DataResourceEntity origin) {
        Object v = (attrs == null) ? null : attrs.get("tenantCode");
        if (v instanceof String s && StringUtils.hasText(s)) {
            return s;
        }
        return origin == null ? null : origin.getTenantCode();
    }

    // ---------------------------
    // Your DB logic (moved here)
    // ---------------------------

    protected List<String> loadAllowedColumns(String resourceCode, String tenantCode) {
        if (!StringUtils.hasText(resourceCode)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<DataResourceEntity> wrapper = Wrappers.lambdaQuery(DataResourceEntity.class);

        wrapper.eq(DataResourceEntity::getResourceParentCode, resourceCode)
                .eq(DataResourceEntity::getResourceType, "FIELD");

        if (StringUtils.hasText(tenantCode)) {
            wrapper.eq(DataResourceEntity::getTenantCode, tenantCode);
        }

        List<DataResourceEntity> entities = resourceMapper.selectList(wrapper);
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> columns = new ArrayList<>(entities.size());
        for (DataResourceEntity e : entities) {
            String code = e.getResourceCode();
            if (StringUtils.hasText(code)) {
                columns.add(code);
            }
        }
        return columns;
    }
}
