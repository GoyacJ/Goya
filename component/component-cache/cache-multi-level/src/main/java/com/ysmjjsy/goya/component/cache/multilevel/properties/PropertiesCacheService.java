package com.ysmjjsy.goya.component.cache.multilevel.properties;

import com.ysmjjsy.goya.component.cache.core.annotation.PropertiesCache;
import com.ysmjjsy.goya.component.cache.core.constants.CacheConst;
import com.ysmjjsy.goya.component.cache.multilevel.template.AbstractCacheTemplate;
import com.ysmjjsy.goya.component.framework.context.SpringContext;
import com.ysmjjsy.goya.component.framework.pojo.PropertiesCacheDTO;
import com.ysmjjsy.goya.component.framework.processor.ApplicationInitializingEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>属性缓存实现</p>
 *
 * @author goya
 * @since 2025/12/29 14:17
 */
@Slf4j
@RequiredArgsConstructor
public class PropertiesCacheService extends AbstractCacheTemplate<String, Object> implements ApplicationListener<ApplicationInitializingEvent> {

    @Override
    protected String getCacheName() {
        return CacheConst.CACHE_PROPERTIES_PREFIX;
    }

    @Override
    public void onApplicationEvent(ApplicationInitializingEvent event) {
        Map<String, Object> beans = SpringContext.getBeanMapsOfAnnotation(PropertiesCache.class);
        beans.values().stream()
                .sorted(Comparator.comparingInt(bean -> bean.getClass()
                        .getAnnotation(PropertiesCache.class).order()))
                .forEach(bean -> {
                    Class<?> clazz = bean.getClass();
                    PropertiesCache annotation = clazz.getAnnotation(PropertiesCache.class);
                    String cacheKey = annotation.cacheKey().isEmpty() ? clazz.getSimpleName() : annotation.cacheKey();
                    getCache().put(cacheKey, bean);
                });
    }

    public <P> P getProperties(Class<P> clazz) {
        PropertiesCache annotation = clazz.getAnnotation(PropertiesCache.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class is not annotated with @CacheOnStartup: " + clazz.getName());
        }

        String cacheKey = annotation.cacheKey().isEmpty() ? clazz.getSimpleName() : annotation.cacheKey();
        P p = getCache().get(cacheKey, clazz);
        if (Objects.isNull(p)) {
            p = SpringContext.getBean(clazz);
        }
        return p;
    }

    public List<PropertiesCacheDTO> getProperties() {
        Map<String, PropertiesCache> beans = SpringContext.getBeanMapsOfType(PropertiesCache.class);
        return beans.values().stream().map(bean -> getPropertiesInfo(bean.getClass())).collect(Collectors.toList());
    }

    public PropertiesCacheDTO getPropertiesInfo(Class<?> clazz) {
        Object bean = getProperties(clazz);
        if (bean == null) {
            return null;
        }

        PropertiesCache propertiesCache = clazz.getAnnotation(PropertiesCache.class);

        Field[] declaredFields = clazz.getDeclaredFields();
        List<PropertiesCacheDTO.PropertiesCacheDetailDTO> details = Arrays.stream(declaredFields).map(field -> {
            field.setAccessible(true);

            Object value;
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

    public <P> void updateProperties(P properties) {
        PropertiesCache annotation = properties.getClass().getAnnotation(PropertiesCache.class);
        String cacheKey = annotation.cacheKey().isEmpty() ? properties.getClass().getSimpleName() : annotation.cacheKey();
        put(cacheKey, properties);
    }
}
