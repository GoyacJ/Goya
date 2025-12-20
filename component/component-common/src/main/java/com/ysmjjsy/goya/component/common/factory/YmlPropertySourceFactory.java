package com.ysmjjsy.goya.component.common.factory;

import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
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
        if (StringUtils.isNotBlank(sourceName) && Strings.CS.endsWithAny(sourceName, ISymbolConstants.SUFFIX_YML, ISymbolConstants.SUFFIX_YAML)) {
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
