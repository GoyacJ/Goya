package com.ysmjjsy.goya.component.captcha.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * <p>字体资源 </p>
 *
 * @author goya
 * @since 2021/12/21 16:00
 */
@Getter
@AllArgsConstructor
public enum CaptchaFontEnum implements Serializable {
    /**
     * 内置字体类型
     */
    ACTION("Action.ttf"),
    BEATAE("Beatae.ttf"),
    EPILOG("Epilog.ttf"),
    FRESNEL("Fresnel.ttf"),
    HEADACHE("Headache.ttf"),
    LEXOGRAPHER("Lexographer.ttf"),
    PREFIX("Prefix"),
    PROG_BOT("ProgBot"),
    ROBOT_TEACHER("RobotTeacher.ttf"),
    SCANDAL("Scandal.ttf");

    private final String fontName;
}
