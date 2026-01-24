package com.ysmjjsy.goya.component.framework.common.error;

import java.util.Arrays;
import java.util.Collection;

/**
 * <p>内置的通用错误码目录</p>
 *
 * @author goya
 * @since 2026/1/24 14:21
 */
public class CommonErrorCodeCatalog implements ErrorCodeCatalog {

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends ErrorCode> codes() {
        return Arrays.asList(CommonErrorCode.values());
    }
}
