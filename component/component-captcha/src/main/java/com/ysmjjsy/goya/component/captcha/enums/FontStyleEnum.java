package com.ysmjjsy.goya.component.captcha.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;
import java.io.Serializable;

/**
 * <p>字体风格 </p>
 * <p>
 * 定义此类的目的：
 * 1. 设置字体风格和字体大小，最初都是使用int类型参数，很容混淆出错，增加个枚举类型以示区别
 * 2. 枚举类型让配置参数配置更便捷。
 *
 * @author goya
 * @since 2021/12/23 10:33
 */
@Getter
@AllArgsConstructor
@Schema(description = "字体风格")
public enum FontStyleEnum implements Serializable {
    /**
     * PLAIN
     */
    @Schema(description = "PLAIN")
    PLAIN(Font.PLAIN),

    /**
     * BOLD
     */
    @Schema(description = "BOLD")
    BOLD(Font.BOLD),

    /**
     * ITALIC
     */
    @Schema(description = "ITALIC")
    ITALIC(Font.ITALIC);

    private final int mapping;
}
