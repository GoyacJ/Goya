package com.ysmjjsy.goya.component.cache.multilevel.configuration.properties;

import com.ysmjjsy.goya.component.cache.core.constants.CacheConst;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>多级缓存配置</p>
 *
 * @author goya
 * @since 2026/1/15 11:38
 */
@ConfigurationProperties(prefix = CacheConst.PROPERTY_MULTI_LEVEL)
public record MultiLevelProperties() {
}
