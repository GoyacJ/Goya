package com.ysmjjsy.goya.component.web.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Sets;
import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import com.ysmjjsy.goya.component.common.definition.enums.IEnum;
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
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
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
        return methods.stream().map(method -> resolve(method.name()).getCode()).collect(Collectors.joining(ISymbolConstants.COMMA));
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
        return Arrays.stream(methods.split(ISymbolConstants.COMMA))
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

    @JsonCreator
    public static RequestMethodEnum fromJson(Object value) {
        // 如果是字符串，直接根据 code 查找
        if (value instanceof String va) {
            return INDEX_MAP.get(va);
        }
        // 如果是 Map 类型，根据 STR_CODE 查找
        if (value instanceof Map<?, ?> map) {
            Object code = map.get(IBaseConstants.STR_CODE);
            return code == null ? null : INDEX_MAP.get(code.toString());
        }

        // 不支持的类型返回 null 或抛异常
        throw new IllegalArgumentException("Unsupported JSON value: " + value);
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }
}
