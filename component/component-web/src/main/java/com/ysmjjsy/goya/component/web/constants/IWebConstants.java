package com.ysmjjsy.goya.component.web.constants;

import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;

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
    String PROPERTY_WEB = IBaseConstants.PROPERTY_PLATFORM + ".web";

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
    String TREE_ROOT_ID = ISymbolConstants.ZERO;
}
