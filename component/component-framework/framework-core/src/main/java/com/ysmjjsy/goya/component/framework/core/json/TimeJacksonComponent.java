package com.ysmjjsy.goya.component.framework.core.json;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.Exceptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.context.annotation.Import;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 23:52
 */
@Slf4j
@JacksonComponent
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Import(TimeJacksonComponent.class)
public class TimeJacksonComponent {

    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD_HH_MM_SS =
            DateTimeFormatter.ofPattern(DefaultConst.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);

    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD =
            DateTimeFormatter.ofPattern(DefaultConst.DATE_FORMAT_YYYY_MM_DD);

    private static final DateTimeFormatter FORMATTER_HHMMSS = DateTimeFormatter.ofPattern(DefaultConst.DATE_FORMAT_HHMMSS);

    /**
     * LocalDateTime 序列化器
     * <p>
     * 将 LocalDateTime 序列化为 yyyy-MM-dd HH:mm:ss 格式的字符串
     */
    public static class LocalDateTimeSerializer extends ValueSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            try {
                String formatted = value.format(FORMATTER_YYYY_MM_DD_HH_MM_SS);
                gen.writeString(formatted);
            } catch (Exception e) {
                log.error("[Goya] |- LocalDateTime 序列化失败: {}", value, e);
                throw Exceptions.system(CommonErrorCode.SYSTEM_JSON_ERROR).cause(e).build();
            }
        }
    }

    /**
     * LocalDateTime 反序列化器
     * <p>
     * 将 yyyy-MM-dd HH:mm:ss 格式的字符串反序列化为 LocalDateTime
     */
    public static class LocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String dateTimeStr = p.getString();

            if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
                return null;
            }

            try {
                return LocalDateTime.parse(dateTimeStr.trim(), FORMATTER_YYYY_MM_DD_HH_MM_SS);
            } catch (DateTimeParseException e) {
                log.warn("[Goya] |- LocalDateTime 反序列化失败: {}, 格式应为: {}",
                        dateTimeStr, DefaultConst.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
                throw Exceptions.system(CommonErrorCode.SYSTEM_JSON_ERROR).cause(e).build();
            }
        }
    }


    /**
     * LocalDate 序列化器
     */
    public static class LocalDateSerializer extends ValueSerializer<LocalDate> {

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            try {
                String formatted = value.format(FORMATTER_YYYY_MM_DD);
                gen.writeString(formatted);
            } catch (Exception e) {
                log.error("[Goya] |- LocalDate 序列化失败: {}", value, e);
                throw Exceptions.system(CommonErrorCode.SYSTEM_JSON_ERROR).cause(e).build();
            }
        }
    }

    /**
     * LocalDate 反序列化器
     */
    public static class LocalDateDeserializer extends ValueDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String dateStr = p.getString();

            if (dateStr == null || dateStr.trim().isEmpty()) {
                return null;
            }

            try {
                return LocalDate.parse(dateStr.trim(), FORMATTER_YYYY_MM_DD);
            } catch (DateTimeParseException e) {
                log.warn("[Goya] |- LocalDate 反序列化失败: {}, 格式应为: {}",
                        dateStr, DefaultConst.DATE_FORMAT_YYYY_MM_DD);
                throw Exceptions.system(CommonErrorCode.SYSTEM_JSON_ERROR).cause(e).build();
            }
        }
    }

    /**
     * LocalTime 序列化器
     */
    public static class LocalTimeSerializer extends ValueSerializer<LocalTime> {

        @Override
        public void serialize(LocalTime value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            try {
                String formatted = value.format(FORMATTER_HHMMSS);
                gen.writeString(formatted);
            } catch (Exception e) {
                log.error("[Goya] |- LocalTime 序列化失败: {}", value, e);
                throw Exceptions.system(CommonErrorCode.SYSTEM_JSON_ERROR).cause(e).build();
            }
        }
    }

    /**
     * LocalTime 反序列化器
     */
    public static class LocalTimeDeserializer extends ValueDeserializer<LocalTime> {

        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String timeStr = p.getString();

            if (timeStr == null || timeStr.trim().isEmpty()) {
                return null;
            }

            try {
                return LocalTime.parse(timeStr.trim(), FORMATTER_HHMMSS);
            } catch (DateTimeParseException e) {
                log.warn("[Goya] |- LocalTime 反序列化失败: {}, 格式应为: HH:mm:ss", timeStr);
                throw Exceptions.system(CommonErrorCode.SYSTEM_JSON_ERROR).cause(e).build();
            }
        }
    }
}
