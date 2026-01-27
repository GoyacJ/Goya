package com.ysmjjsy.goya.component.framework.oss.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ysmjjsy.goya.component.framework.core.enums.PropertyEnum;
import com.ysmjjsy.goya.component.framework.oss.constants.OssConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Oss枚举</p>
 *
 * @author goya
 * @since 2025/10/9 16:45
 */
@Getter
@AllArgsConstructor
@Schema(description = "Oss枚举")
public enum OssEnum implements PropertyEnum {

    /**
     * minio
     */
    MINIO("MINIO", "minio"),

    /**
     * 国密加密算法
     */
    ALIYUN("ALIYUN", "aliyun"),

    /**
     * s3
     */
    S3("S3", "s3"),

    ;

    @JsonValue
    private final String code;
    private final String label;

    private static final Map<String, OssEnum> INDEX_MAP = Maps.newHashMap();
    private static final List<Map<String, Object>> JSON_STRUCT = new ArrayList<>();

    static {
        for (OssEnum anEnum : OssEnum.values()) {
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
    public static OssEnum getByCode(String code) {
        return INDEX_MAP.get(code);
    }

    public static List<Map<String, Object>> getJsonStruct() {
        return JSON_STRUCT;
    }

    @Override
    public String getPrefix() {
        return OssConstants.PROPERTY_PREFIX_OSS_TYPE;
    }
}
