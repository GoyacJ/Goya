package com.ysmjjsy.goya.component.core.mapstruct;

import org.mapstruct.BeanMapping;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 00:07
 */
@MapperConfig
public interface MapStructConverter<O, T> {

    /**
     * Origin -> Target
     * @return T
     */
    T toTarget(O origin);

    /**
     * Target -> Origin
     * @return O
     */
    O toOrigin(T target);

    /**
     * 批量 Target -> Origin
     * @return List<T>
     */
    List<T> toTargetList(List<O> originList);

    /**
     * 批量 Origin -> Target
     * @return List<O>
     */
    List<O> toOriginList(List<T> targetList);

    /**
     * Target -> Origin 增量更新（忽略 null）
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(T target, @MappingTarget O origin);
}
