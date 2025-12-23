package com.ysmjjsy.goya.component.common.definition.pojo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * <p>配置缓存信息</p>
 *
 * @author goya
 * @since 2025/12/23 23:40
 */

@Schema(description = "配置缓存信息")
public record PropertiesCacheDTO(

        @Schema(description = "缓存Key名称")
        String cacheKey,

        @Schema(description = "配置类")
        String configClass,

        @Schema(description = "配置类名称")
        String configName,

        @Schema(description = "配置项列表")
        List<PropertiesCacheDetailDTO> fields
) {

    @Schema(description = "配置项信息")
    public record PropertiesCacheDetailDTO(

            @Schema(description = "字段")
            String field,

            @Schema(description = "字段名称,取 @Schema 注解的 description")
            String fieldName,

            @Schema(description = "字段值")
            Object fieldValue,

            @Schema(description = "类型信息")
            String type
    ) {

    }
}

