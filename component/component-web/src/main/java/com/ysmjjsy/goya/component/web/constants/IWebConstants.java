package com.ysmjjsy.goya.component.web.constants;

import com.google.common.collect.Lists;
import com.ysmjjsy.goya.component.core.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.constants.PropertyConst;

import java.util.List;

import static com.ysmjjsy.goya.component.cache.core.constants.CacheConst.CACHE_PREFIX;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 19:01
 */
public interface IWebConstants {

    /**
     * 配置前缀
     * 配置示例: platform.web
     */
    String PROPERTY_WEB = PropertyConst.PROPERTY_GOYA + ".web";

    /**
     * 缓存属性前缀
     */
    String CACHE_WEB_PREFIX = CACHE_PREFIX + "web:";
    String CACHE_ACCESS_LIMITED_PREFIX = CACHE_WEB_PREFIX + "access_limited:";
    String CACHE_IDEMPOTENT_PREFIX = CACHE_WEB_PREFIX + "idempotent:";


    // ====================== Header ======================

    /**
     * HEADER_REQUEST_ID
     */
    String HEADER_REQUEST_ID = "H-Request-Id";

    /**
     * HEADER_TENANT_ID
     */
    String HEADER_TENANT_ID = "H-Tenant-Id";

    /**
     * HEADER_OPEN_ID
     */
    String HEADER_OPEN_ID = "H-Open-Id";

    /**
     * HEADER_INNER
     */
    String HEADER_INNER = "H-Inner";


    /**
     * 默认树形结构根节点
     */
    String TREE_ROOT_ID = SymbolConst.ZERO;

    String MATCHER_WEBJARS = "/webjars/**";
    String MATCHER_STATIC = "/static/**";

    List<String> DEFAULT_IGNORED_STATIC_RESOURCES = Lists.newArrayList(MATCHER_WEBJARS, MATCHER_STATIC,
            "/error/**",
            "/goya/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/openapi.json",
            "/favicon.ico",
            "/.well-known/**"
    );
    List<String> DEFAULT_PERMIT_ALL_RESOURCES = Lists.newArrayList(
            "/open/**",
            "/stomp/ws",
            "/oauth2/sign-out",
            "/login*"
    );
}
