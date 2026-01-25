package com.ysmjjsy.goya.component.framework.core.context;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/16 16:21
 */
@Slf4j
public class TenantContext {


    private static final ThreadLocal<String> CURRENT_TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TENANT_CODE = new ThreadLocal<>();

    /**
     * 默认租户ID
     */
    public static final String DEFAULT_TENANT_ID = DefaultConst.DEFAULT_TENANT_ID;

    /**
     * 默认租户编码
     */
    public static final String DEFAULT_TENANT_CODE = "default";

    /**
     * 设置当前租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(String tenantId) {
        CURRENT_TENANT_ID.set(tenantId);
        log.debug("设置当前租户ID: {}", tenantId);
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID，如果未设置则返回默认租户ID
     */
    public static String getTenantId() {
        String tenantId = CURRENT_TENANT_ID.get();
        return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
    }

    /**
     * 设置当前租户编码
     *
     * @param tenantCode 租户编码
     */
    public static void setTenantCode(String tenantCode) {
        CURRENT_TENANT_CODE.set(tenantCode);
        log.debug("设置当前租户编码: {}", tenantCode);
    }

    /**
     * 获取当前租户编码
     *
     * @return 租户编码，如果未设置则返回默认租户编码
     */
    public static String getTenantCode() {
        String tenantCode = CURRENT_TENANT_CODE.get();
        return tenantCode != null ? tenantCode : DEFAULT_TENANT_CODE;
    }

    /**
     * 清除当前租户上下文
     */
    public static void clear() {
        CURRENT_TENANT_ID.remove();
        CURRENT_TENANT_CODE.remove();
        log.debug("清除租户上下文");
    }

    public static boolean isCaptchaEnabled(String tenantId, String loginType) {
        return false;
    }
}
