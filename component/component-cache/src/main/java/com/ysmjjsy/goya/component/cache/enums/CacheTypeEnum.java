package com.ysmjjsy.goya.component.cache.enums;

import com.ysmjjsy.goya.component.cache.constants.ICacheConstants;
import com.ysmjjsy.goya.component.common.definition.enums.IPropertyEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>缓存类型枚举</p>
 * <p>定义系统支持的缓存类型，包括本地缓存和分布式缓存</p>
 *
 * @author goya
 * @since 2025/12/21 23:30
 * @see ICacheConstants
 */
@Getter
@AllArgsConstructor
@Schema(description = "缓存类型枚举")
public enum CacheTypeEnum implements IPropertyEnum {

    /**
     * CAFFEINE
     */
    @Schema(description = "CAFFEINE")
    CAFFEINE("CAFFEINE", "CAFFEINE"),

    /**
     * REDIS
     */
    @Schema(description = "REDIS")
    REDIS("REDIS", "REDIS"),

    ;

    @Schema(description = "状态编码")
    private final String code;

    @Schema(description = "状态描述")
    private final String description;

    @Override
    public String getPrefix() {
        return ICacheConstants.PROPERTY_CACHE_TYPE;
    }
    /**
     * 根据代码获取枚举实例
     *
     * @param code 缓存类型代码
     * @return 对应的枚举实例，不存在则返回 null
     */
    public static CacheTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (CacheTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断代码是否有效
     *
     * @param code 缓存类型代码
     * @return 如果代码对应的枚举存在则返回 true，否则返回 false
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}

