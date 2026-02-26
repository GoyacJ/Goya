package com.ysmjjsy.goya.component.admin.constants;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;

/**
 * <p>admin 常量定义</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public interface AdminConst {

    String PROPERTY_ADMIN = PropertyConst.PROPERTY_GOYA + ".admin";

    String TENANT_HEADER = "X-Tenant-Id";

    String DEFAULT_TENANT_ID = "public";

    String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    String POLICY_ACTION_ACCESS = "ACCESS";
}
