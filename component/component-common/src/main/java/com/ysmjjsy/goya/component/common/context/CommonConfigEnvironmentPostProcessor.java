package com.ysmjjsy.goya.component.common.context;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:42
 */
@Slf4j
public class CommonConfigEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String COMMON_CONFIG_PATTERN = "classpath*:common/*.{yml,yaml}";

    @Override
    public void postProcessEnvironment(@NonNull ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(COMMON_CONFIG_PATTERN);

            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

            for (Resource resource : resources) {
                if (resource.exists()) {
                    String name = resource.getFilename();
                    List<PropertySource<?>> sources = null;
                    if (name != null) {
                        sources = loader.load(name, resource);
                    }
                    if (sources != null) {
                        for (PropertySource<?> ps : sources) {
                            // 放在最后，优先级最低，让用户 application.yml 可以覆盖
                            environment.getPropertySources().addLast(ps);
                        }
                    }
                    log.info("Loaded common config: {}", name);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load common configs from " + COMMON_CONFIG_PATTERN, e);
        }
    }
}
