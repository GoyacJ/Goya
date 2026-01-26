package com.ysmjjsy.goya.component.captcha.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ysmjjsy.goya.component.core.enums.IEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/9/30 17:03
 */
@Getter
@Schema(name = "验证码类别")
@AllArgsConstructor
public enum CaptchaCategoryEnum implements IEnum<String> {

    /**
     * 验证码类别
     */
    JIGSAW(CaptchaCategoryEnum.JIGSAW_CAPTCHA, "滑块拼图验证码"),
    WORD_CLICK(CaptchaCategoryEnum.WORD_CLICK_CAPTCHA, "文字点选验证码"),
    ARITHMETIC(CaptchaCategoryEnum.ARITHMETIC_CAPTCHA, "算数类型验证码"),
    CHINESE(CaptchaCategoryEnum.CHINESE_CAPTCHA, "中文类型验证码"),
    CHINESE_GIF(CaptchaCategoryEnum.CHINESE_GIF_CAPTCHA, "中文GIF类型验证码"),
    SPEC_GIF(CaptchaCategoryEnum.SPEC_GIF_CAPTCHA, "GIF类型验证码"),
    SPEC(CaptchaCategoryEnum.SPEC_CAPTCHA, "PNG类型验证码"),
    HUTOOL_LINE(CaptchaCategoryEnum.HUTOOL_LINE_CAPTCHA, "Hutool线段干扰验证码"),
    HUTOOL_CIRCLE(CaptchaCategoryEnum.HUTOOL_CIRCLE_CAPTCHA, "Hutool圆圈干扰验证码"),
    HUTOOL_SHEAR(CaptchaCategoryEnum.HUTOOL_SHEAR_CAPTCHA, "Hutool扭曲干扰验证码"),
    HUTOOL_GIF(CaptchaCategoryEnum.HUTOOL_GIF_CAPTCHA, "Hutool GIF验证码");

    public static final String JIGSAW_CAPTCHA = "JIGSAW";
    public static final String WORD_CLICK_CAPTCHA = "WORD_CLICK";
    public static final String ARITHMETIC_CAPTCHA = "ARITHMETIC";
    public static final String CHINESE_CAPTCHA = "CHINESE";
    public static final String CHINESE_GIF_CAPTCHA = "CHINESE_GIF";
    public static final String SPEC_CAPTCHA = "SPEC";
    public static final String SPEC_GIF_CAPTCHA = "SPEC_GIF";
    public static final String HUTOOL_LINE_CAPTCHA = "HUTOOL_LINE";
    public static final String HUTOOL_CIRCLE_CAPTCHA = "HUTOOL_CIRCLE";
    public static final String HUTOOL_SHEAR_CAPTCHA = "HUTOOL_SHEAR";
    public static final String HUTOOL_GIF_CAPTCHA = "HUTOOL_GIF";

    private static final Map<String, CaptchaCategoryEnum> INDEX_MAP = new HashMap<>();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (CaptchaCategoryEnum anEnum : CaptchaCategoryEnum.values()) {
            INDEX_MAP.put(anEnum.getCode(), anEnum);
            JSON_STRUCT.add(anEnum.ordinal(),
                    Map.of(
                            "index", anEnum.ordinal(),
                            "code", anEnum.getCode(),
                            "name", anEnum.name(),
                            "description", anEnum.getDescription()
                    ));
        }
    }

    @Schema(name = "常量值")
    @JsonValue
    private final String code;
    @Schema(name = "文字")
    private final String description;

    @JsonCreator
    public static CaptchaCategoryEnum getByCode(String code) {
        return INDEX_MAP.get(code);
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }

}
