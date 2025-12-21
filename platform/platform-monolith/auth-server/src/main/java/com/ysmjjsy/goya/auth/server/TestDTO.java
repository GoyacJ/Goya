package com.ysmjjsy.goya.auth.server;

import com.ysmjjsy.goya.component.common.annotation.ArrayDelimited;

import java.time.LocalDateTime;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 23:59
 */
public record TestDTO(
        String id,
        LocalDateTime time,

        @ArrayDelimited
        String params
) {
}
