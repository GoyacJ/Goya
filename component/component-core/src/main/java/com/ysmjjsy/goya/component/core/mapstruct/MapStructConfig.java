package com.ysmjjsy.goya.component.core.mapstruct;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 00:08
 */
@MapperConfig(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.WARN,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MapStructConfig {
}
