package com.ysmjjsy.goya.component.common.service;

import com.ysmjjsy.goya.component.common.configuration.properties.PlatformInfo;
import com.ysmjjsy.goya.component.common.configuration.properties.PlatformProperties;
import com.ysmjjsy.goya.component.common.context.SpringContext;
import com.ysmjjsy.goya.component.common.definition.constants.IRegexPoolConstants;
import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

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

    /**
     * 获取端口
     *
     * @return 端口信息
     */
    Integer getPort();

    /**
     * 获取Ip
     *
     * @return ip信息
     */
    String getIp();

    /**
     * 获取地址
     *
     * @return 地址信息
     */
    default String getAddress() {
        return this.getIp() + ISymbolConstants.COLON + this.getPort();
    }

    /**
     * 获取Url
     *
     * @return url
     */
    String getUrl();

    /**
     * Web上下文路径
     *
     * @return Web上下文路径
     */
    String getContextPath();

    /**
     * 是否存在ContextPath
     *
     * @return 结果
     */
    default boolean hasContextPath() {
        return StringUtils.hasText(getContextPath());
    }
}
