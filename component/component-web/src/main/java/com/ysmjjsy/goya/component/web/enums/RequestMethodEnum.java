package com.ysmjjsy.goya.component.web.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Sets;
import com.ysmjjsy.goya.component.core.constants.SymbolConst;
import com.ysmjjsy.goya.component.core.enums.IEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/10 22:28
 */
@Getter
@AllArgsConstructor
@Schema(description = "Http Request Method")
public enum RequestMethodEnum implements IEnum<String> {

    GET("GET", "GET"),
    HEAD("HEAD", "HEAD"),
    POST("POST", "POST"),
    PUT("PUT", "PUT"),
    PATCH("PATCH", "PATCH"),
    DELETE("DELETE", "DELETE"),
    OPTIONS("OPTIONS", "OPTIONS"),
    TRACE("TRACE", "TRACE"),

    ALL("ALL", "ALL"),

    ;

    @JsonValue
    private final String code;
    private final String description;
    private static final Map<String, RequestMethodEnum> INDEX_MAP = new HashMap<>();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (RequestMethodEnum anEnum : RequestMethodEnum.values()) {
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

    @JsonCreator
    public static RequestMethodEnum getByCode(String code) {
        return INDEX_MAP.get(code);
    }

    public static RequestMethodEnum resolve(String code) {
        if (StringUtils.isBlank(code)) {
            return ALL;
        }
        return getByCode(code);
    }

    public static String handlerRequestMethods(Set<RequestMethod> methods) {
        if (methods.isEmpty()) {
            return ALL.getCode();
        }
        return methods.stream().map(method -> resolve(method.name()).getCode()).collect(Collectors.joining(SymbolConst.COMMA));
    }

    public static Set<RequestMethodEnum> parseRequestMethods(Set<RequestMethod> methods) {
        if (methods.isEmpty()) {
            return Sets.newHashSet(ALL);
        }
        return methods.stream().map(method -> resolve(method.name())).collect(Collectors.toSet());
    }

    public static Set<RequestMethodEnum> parseRequestMethods(String methods) {
        if (StringUtils.isBlank(methods)) {
            return Sets.newHashSet(ALL);
        }
        return Arrays.stream(methods.split(SymbolConst.COMMA))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(s -> {
                    try {
                        return RequestMethodEnum.resolve(s);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
