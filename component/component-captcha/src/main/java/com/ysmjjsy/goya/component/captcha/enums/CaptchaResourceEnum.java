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
 * <p>验证码资源 </p>
 *
 * @author goya
 * @since 2021/12/11 15:27
 */
@Getter
@Schema(name = "验证码资源")
@AllArgsConstructor
public enum CaptchaResourceEnum implements IEnum<String> {

    /**
     * 验证码资源类型
     */
    JIGSAW_ORIGINAL("Jigsaw original image", "滑动拼图底图"),
    JIGSAW_TEMPLATE("Jigsaw template image", "滑动拼图滑块底图"),
    WORD_CLICK("Word click image", "文字点选底图");

    private static final Map<String, CaptchaResourceEnum> INDEX_MAP = new HashMap<>();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (CaptchaResourceEnum anEnum : CaptchaResourceEnum.values()) {
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
    public static CaptchaResourceEnum getByCode(String code) {
        return INDEX_MAP.get(code);
    }


    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
