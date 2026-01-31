package com.ysmjjsy.goya.component.mybatisplus.permission.converter;

import com.ysmjjsy.goya.component.framework.common.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.core.mapstruct.MapStructConfig;
import com.ysmjjsy.goya.component.framework.core.mapstruct.MapStructConverter;
import com.ysmjjsy.goya.component.framework.security.domain.Action;
import com.ysmjjsy.goya.component.framework.security.domain.Policy;
import com.ysmjjsy.goya.component.mybatisplus.permission.entity.DataResourcePolicyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/31 22:07
 */
@Mapper(config = MapStructConfig.class)
public interface PolicyConverter extends MapStructConverter<DataResourcePolicyEntity, Policy> {

    @Override
    @Mapping(target = "policyId", source = "id")
    @Mapping(target = "action", source = "actionCode", qualifiedByName = "codeToAction")
    @Mapping(target = "allowColumns", source = "allowColumns", qualifiedByName = "csvToList")
    @Mapping(target = "denyColumns", source = "denyColumns", qualifiedByName = "csvToList")
    @Mapping(target = "attributes", expression = "java(new HashMap<>())")
    Policy toTarget(DataResourcePolicyEntity origin);

    @Override
    @Mapping(target = "actionCode", source = "action", qualifiedByName = "actionToCode")
    DataResourcePolicyEntity toOrigin(Policy target);

    /**
     * CSV -> List<String>
     */
    @Named("csvToList")
    default List<String> csvToList(String csv) {
        if (!StringUtils.hasText(csv)) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(SymbolConst.COMMA))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    /**
     * actionCode -> Action
     */
    @Named("codeToAction")
    default Action codeToAction(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        Action action = new Action();
        action.setCode(code.trim());
        return action;
    }

    /**
     * Action -> actionCode
     */
    @Named("actionToCode")
    default String actionToCode(Action action) {
        if (action == null || !StringUtils.hasText(action.getCode())) {
            return null;
        }
        return action.getCode().trim();
    }
}
