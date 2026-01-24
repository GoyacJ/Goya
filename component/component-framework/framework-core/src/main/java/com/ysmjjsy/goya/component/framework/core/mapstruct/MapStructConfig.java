package com.ysmjjsy.goya.component.framework.core.mapstruct;

import com.ysmjjsy.goya.component.framework.core.enums.CodeEnumMapStructMapper;
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
        uses = {CodeEnumMapStructMapper.class},
        unmappedTargetPolicy = ReportingPolicy.WARN,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MapStructConfig {
}
