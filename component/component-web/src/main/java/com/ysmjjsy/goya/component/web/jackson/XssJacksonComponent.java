package com.ysmjjsy.goya.component.web.jackson;

import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import com.ysmjjsy.goya.component.web.utils.XssUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.format.DateTimeParseException;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 17:49
 */
@Slf4j
@JacksonComponent
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XssJacksonComponent {

    /**
     * Xss 反序列化器
     */
    public static class XssDeserializer extends ValueDeserializer<String> {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
            String value = p.getString();

            try {
                if (StringUtils.isNotBlank(value)) {
                    return XssUtils.process(value);
                }

                return value;
            } catch (DateTimeParseException e) {
                throw new CommonException(
                        "Xss 反序列化错误", e);
            }
        }
    }
}
