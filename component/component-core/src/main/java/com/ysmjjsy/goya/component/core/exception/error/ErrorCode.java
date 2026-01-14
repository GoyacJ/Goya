package com.ysmjjsy.goya.component.core.exception.error;

import com.ysmjjsy.goya.component.core.enums.IBizEnum;

/**
 * <p>error code</p>
 *
 * @author goya
 * @since 2026/1/7 22:54
 */
public interface ErrorCode extends IBizEnum<String> {

    /**
     * 国际化
     *
     * @return 国际化值
     */
    @Override
    default String getI18nKey() {
        return "response.code." + getCode();
    }
}
