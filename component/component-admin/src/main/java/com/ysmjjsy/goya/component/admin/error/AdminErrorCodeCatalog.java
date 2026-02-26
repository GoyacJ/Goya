package com.ysmjjsy.goya.component.admin.error;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCodeCatalog;

import java.util.Arrays;
import java.util.Collection;

/**
 * <p>admin 错误码目录</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public class AdminErrorCodeCatalog implements ErrorCodeCatalog {

    @Override
    public Collection<? extends ErrorCode> codes() {
        return Arrays.asList(AdminErrorCode.values());
    }
}
