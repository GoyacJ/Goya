package com.ysmjjsy.goya.component.common.service;

import com.ysmjjsy.goya.component.common.configuration.properties.PlatformInfo;
import com.ysmjjsy.goya.component.common.configuration.properties.PlatformProperties;
import com.ysmjjsy.goya.component.common.context.SpringContext;
import com.ysmjjsy.goya.component.common.definition.constants.IRegexPoolConstants;
import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>平台公共能力抽取</p>
 *
 * @author goya
 * @since 2025/12/21 22:23
 */
public interface IPlatformService {

    /**
     * 获取平台配置
     *
     * @return PlatformProperties
     */
    default PlatformProperties getPlatformProperties() {
        return SpringContext.getBean(PlatformProperties.class);
    }

    /**
     * 获取平台信息
     *
     * @return PlatformInfo
     */
    default PlatformInfo getPlatformInfo() {
        return getPlatformProperties().platformInfo();
    }

    /**
     * 获取类包名集合
     *
     * @return Set<String>
     */
    static Set<String> getPackageNames() {
        Set<String> packageNames = new LinkedHashSet<>();

        String rootPackage = Arrays.stream(
                        IPlatformService.class.getPackageName().split(IRegexPoolConstants.PACKAGE_SEPARATOR_REGEX))
                .limit(3)
                .collect(Collectors.joining(ISymbolConstants.PERIOD));
        packageNames.add(rootPackage);

        ApplicationContext context = SpringContext.getApplicationContext();

        if (Objects.nonNull(context)) {

            PlatformProperties properties = SpringContext.getBean(PlatformProperties.class);
            if (Objects.nonNull(properties)) {
                packageNames.add(properties.platformInfo().baskPackageName());
            }

            // Spring Boot 主扫描包
            if (AutoConfigurationPackages.has(context)) {
                packageNames.addAll(AutoConfigurationPackages.get(context));
            }
        }
        return packageNames;
    }


}
