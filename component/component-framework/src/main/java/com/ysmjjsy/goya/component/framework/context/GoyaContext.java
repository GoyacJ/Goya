package com.ysmjjsy.goya.component.framework.context;

import com.ysmjjsy.goya.component.core.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.configuration.properties.GoyaProperties;
import com.ysmjjsy.goya.component.framework.configuration.properties.PlatformInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/16 15:36
 */
public interface GoyaContext {

    /**
     * 获取平台配置
     *
     * @return PlatformProperties
     */
    default GoyaProperties getProperties() {
        return SpringContext.getBean(GoyaProperties.class);
    }

    /**
     * 获取平台信息
     *
     * @return PlatformInfo
     */
    default PlatformInfo getPlatformInfo() {
        return getProperties().platformInfo();
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
        return this.getIp() + SymbolConst.COLON + this.getPort();
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

    /**
     * 获取认证中心地址
     *
     * @return 认证中心地址
     */
    String getAuthServiceUri();

    /**
     * 获取认证中心名称
     *
     * @return 认证中心名称
     */
    String getAuthServiceName();

    /**
     * 当前用户
     *
     * @return GoyaUser
     */
    GoyaUser currentUser();

    /**
     * 当前用户
     *
     * @param request request
     * @return GoyaUser
     */
    GoyaUser currentUser(HttpServletRequest request);

    /**
     * 当前用户
     *
     * @param token token
     * @return GoyaUser
     */
    GoyaUser currentUser(String token);

    /**
     * 当前租户
     *
     * @return 租户Id
     */
    String currentTenant();

}
