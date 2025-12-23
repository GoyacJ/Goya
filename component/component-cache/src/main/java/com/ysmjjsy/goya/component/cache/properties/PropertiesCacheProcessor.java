package com.ysmjjsy.goya.component.cache.properties;

import com.ysmjjsy.goya.component.cache.annotation.PropertiesCache;
import com.ysmjjsy.goya.component.cache.service.ICacheService;
import com.ysmjjsy.goya.component.common.context.ApplicationInitializingEvent;
import com.ysmjjsy.goya.component.common.context.SpringContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

import java.util.Comparator;
import java.util.Map;

/**
 * <p>配置缓存处理器</p>
 *
 * @author goya
 * @since 2025/12/23 23:34
 */
@Slf4j
@RequiredArgsConstructor
public class PropertiesCacheProcessor implements ApplicationListener<ApplicationInitializingEvent> {

    private final ICacheService iCacheService;
    private static final String PROPERTIES_CACHE_NAME = "properties:";

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
                    iCacheService.putEternal(PROPERTIES_CACHE_NAME, cacheKey, bean);
                });
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String cacheKey, Class<T> clazz) {
        Object value = iCacheService.get(PROPERTIES_CACHE_NAME, cacheKey);
        if (value == null) {
            return null;
        }
        if (!clazz.isInstance(value)) {
            throw new IllegalStateException("Cached object is not of type " + clazz.getName());
        }
        return (T) value;
    }

    public Map<String, Object> getProperties() {
        Map<String, PropertiesCache> beans = SpringContext.getBeanMapsOfType(PropertiesCache.class);
        // ICache接口没有getAll方法，这里需要手动获取
        Map<String, Object> result = new java.util.HashMap<>();
        for (String key : beans.keySet()) {
            Object value = iCacheService.get(PROPERTIES_CACHE_NAME, key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    public <T> void putProperty(String cacheKey, T bean) {
        iCacheService.putEternal(PROPERTIES_CACHE_NAME, cacheKey, bean);
    }
}
