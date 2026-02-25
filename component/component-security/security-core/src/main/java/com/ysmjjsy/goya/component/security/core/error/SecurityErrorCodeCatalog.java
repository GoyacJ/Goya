package com.ysmjjsy.goya.component.security.core.error;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCodeCatalog;

import java.util.Arrays;
import java.util.Collection;

/**
 * <p>Security 错误码目录</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class SecurityErrorCodeCatalog implements ErrorCodeCatalog {

    @Override
    public Collection<? extends ErrorCode> codes() {
        return Arrays.asList(SecurityErrorCode.values());
    }
}
