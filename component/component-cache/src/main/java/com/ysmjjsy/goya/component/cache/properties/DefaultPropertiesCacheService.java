package com.ysmjjsy.goya.component.cache.properties;

import com.ysmjjsy.goya.component.cache.annotation.PropertiesCache;
import com.ysmjjsy.goya.component.common.context.SpringContext;
import com.ysmjjsy.goya.component.common.definition.pojo.PropertiesCacheDTO;
import com.ysmjjsy.goya.component.common.service.IPropertiesCacheService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>默认缓存配置</p>
 *
 * @author goya
 * @since 2025/12/23 23:40
 */
@Slf4j
@SuppressWarnings("all")
@RequiredArgsConstructor
public class DefaultPropertiesCacheService implements IPropertiesCacheService {

    private final PropertiesCacheProcessor propertiesCacheProcessor;

    /**
     * 简化方式，根据类名作为默认 cacheKey
     *
     * @param clazz 配置对象类型
     * @param <T>   泛型类型
     * @return 配置对象
     */
    @Override
    public <T> T getProperties(Class<T> clazz) {
        PropertiesCache annotation = clazz.getAnnotation(PropertiesCache.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class is not annotated with @CacheOnStartup: " + clazz.getName());
        }

        String cacheKey = annotation.cacheKey().isEmpty() ? clazz.getSimpleName() : annotation.cacheKey();
        T property = propertiesCacheProcessor.getProperty(cacheKey, clazz);
        if (Objects.isNull(property)) {
            return SpringContext.getBean(clazz);
        }
        return property;
    }

    @Override
    public List<PropertiesCacheDTO> getProperties() {
        Map<String, PropertiesCache> beans = SpringContext.getBeanMapsOfType(PropertiesCache.class);
        return beans.values().stream().map(bean -> getPropertiesInfo(bean.getClass())).collect(Collectors.toList());
    }

    @Override
    public PropertiesCacheDTO getPropertiesInfo(Class<?> clazz) {
        Object bean = getProperties(clazz);
        if (bean == null) {
            return null;
        }

        PropertiesCache propertiesCache = clazz.getAnnotation(PropertiesCache.class);

        Field[] declaredFields = clazz.getDeclaredFields();
        List<PropertiesCacheDTO.PropertiesCacheDetailDTO> details = Arrays.stream(declaredFields).map(field -> {
            field.setAccessible(true);

            Object value = null;
            try {
                value = field.get(bean);
            } catch (IllegalAccessException e) {
                log.warn("配置获取失败.", e);
                return null;
            }

            Schema fieldSchema = field.getAnnotation(Schema.class);

            return new PropertiesCacheDTO.PropertiesCacheDetailDTO(
                    field.getName(),
                    fieldSchema != null ? fieldSchema.description() : field.getName(),
                    value,
                    field.getType().getSimpleName()
            );
        }).filter(Objects::nonNull).toList();

        Schema schema = clazz.getAnnotation(Schema.class);
        return new PropertiesCacheDTO(
                propertiesCache.cacheKey().isEmpty() ? clazz.getSimpleName() : propertiesCache.cacheKey(),
                clazz.getSimpleName(),
                schema != null ? schema.description() : clazz.getSimpleName(),
                details
        );
    }

    @Override
    public <P> void updateProperties(P properties) {
        PropertiesCache annotation = properties.getClass().getAnnotation(PropertiesCache.class);
        String cacheKey = annotation.cacheKey().isEmpty() ? properties.getClass().getSimpleName() : annotation.cacheKey();
        propertiesCacheProcessor.putProperty(cacheKey, properties);
    }
}
