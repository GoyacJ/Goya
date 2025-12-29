package com.ysmjjsy.goya.component.common.service;

import com.ysmjjsy.goya.component.common.definition.pojo.PropertiesCacheDTO;

import java.util.List;

/**
 * <p>属性缓存</p>
 *
 * @author goya
 * @since 2025/12/23 23:39
 */
public interface IPropertiesCacheService {


    /**
     * 简化方式，根据类名作为默认 cacheKey
     *
     * @param clazz 配置对象类型
     * @param <P>   泛型类型
     * @return 配置对象
     */
    <P> P getProperties(Class<P> clazz);

    /**
     * 获取所有配置对象
     *
     * @return 所有配置对象的 Map
     */
    List<PropertiesCacheDTO> getProperties();

    /**
     * 获取配置详细信息
     *
     * @param clazz 类型
     * @return 详细信息
     */
    PropertiesCacheDTO getPropertiesInfo(Class<?> clazz);

    /**
     * 更新配置
     *
     * @param properties  配置
     * @param <P>       配置
     */
    <P> void updateProperties(P properties);
}
