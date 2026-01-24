package com.ysmjjsy.goya.component.framework.servlet.context;

import java.io.Serializable;
import java.security.Principal;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/16 17:12
 */
public interface GoyaUser extends Principal, Serializable {

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    String getUserId();
}
