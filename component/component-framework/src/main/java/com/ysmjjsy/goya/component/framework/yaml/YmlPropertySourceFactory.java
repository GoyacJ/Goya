package com.ysmjjsy.goya.component.framework.yaml;

import com.ysmjjsy.goya.component.core.constants.SymbolConst;
import com.ysmjjsy.goya.component.core.utils.GoyaStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

/**
 * <p>yml 配置源工厂</p>
 *
 * @author goya
 * @since 2025/12/20 00:04
 */
@Slf4j
public class YmlPropertySourceFactory extends DefaultPropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource resource) throws IOException {
        String sourceName = resource.getResource().getFilename();
        log.debug("[Goya] |- createPropertySource: {}", sourceName);
        if (GoyaStringUtils.isNotBlank(sourceName) && GoyaStringUtils.endsWithAnyIgnoreCase(sourceName, SymbolConst.SUFFIX_YML, SymbolConst.SUFFIX_YAML)) {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            log.debug("[Goya] |- createPropertySource: resource: {}", resource.getResource());
            log.debug("[Goya] |- createPropertySource: factory: {}", factory.getObject().toString());
            factory.afterPropertiesSet();
            return new PropertiesPropertySource(sourceName, factory.getObject());
        }
        return super.createPropertySource(name, resource);
    }
}
