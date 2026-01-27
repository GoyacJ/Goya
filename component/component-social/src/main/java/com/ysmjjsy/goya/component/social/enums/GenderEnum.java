package com.ysmjjsy.goya.component.social.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.zhyd.oauth.enums.AuthUserGender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/21 10:46
 */
@Getter
@AllArgsConstructor
@Schema(description = "性别")
public enum GenderEnum implements CodeEnum<String> {

    /**
     * enum
     */
    MAN("MAN", "男"),
    WOMAN("WOMAN", "女"),
    OTHER("OTHER", "其它"),

    ;

    @JsonValue
    private final String code;
    private final String label;

    private static final Map<String, GenderEnum> INDEX_MAP = Maps.newHashMap();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (GenderEnum anEnum : GenderEnum.values()) {
            INDEX_MAP.put(anEnum.getCode(), anEnum);
            JSON_STRUCT.add(anEnum.ordinal(),
                    ImmutableMap.<String, Object>builder()
                            .put("index", anEnum.ordinal())
                            .put("code", anEnum.getCode())
                            .put("label", anEnum.getLabel())
                            .build());
        }
    }

    @JsonCreator
    public static GenderEnum getByCode(String code) {
        return INDEX_MAP.get(code);
    }


    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }

    public static GenderEnum convert(AuthUserGender authUserGender){
        switch (authUserGender){
            case MALE -> {
                return GenderEnum.MAN;
            }
            case FEMALE ->  {
                return GenderEnum.WOMAN;
            }
            case UNKNOWN ->  {
                return GenderEnum.OTHER;
            }
            case null ->   {
                return GenderEnum.OTHER;
            }
        }
    }
}
