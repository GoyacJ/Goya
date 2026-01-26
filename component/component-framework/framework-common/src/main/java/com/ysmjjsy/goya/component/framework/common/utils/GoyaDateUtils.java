package com.ysmjjsy.goya.component.framework.common.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/9/24 14:23
 */
@UtilityClass
public class GoyaDateUtils {

    public static String zonedDateTimeToString(ZonedDateTime zonedDateTime) {
        return zonedDateTimeToString(zonedDateTime, "yyyy-MM-dd HH:mm:ss");
    }

    public static LocalDateTime zonedDateTimeToLocalDateTime(ZonedDateTime zonedDateTime) {
        return ObjectUtils.isNotEmpty(zonedDateTime) ? LocalDateTime.ofInstant(zonedDateTime.toInstant(), ZoneId.systemDefault()) : LocalDateTime.now();
    }

    public static String zonedDateTimeToString(ZonedDateTime zonedDateTime, String format) {
        if (ObjectUtils.isNotEmpty(zonedDateTime) && StringUtils.isNotBlank(format)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("Asia/Shanghai"));
            return zonedDateTime.format(formatter);
        } else {
            return null;
        }
    }

    public static ZonedDateTime stringToZonedDateTime(String dateString) {
        return stringToZonedDateTime(dateString, "yyyy-MM-dd HH:mm:ss");
    }

    public static ZonedDateTime stringToZonedDateTime(String dateString, String format) {
        if (StringUtils.isNotBlank(dateString) && StringUtils.isNotBlank(format)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("Asia/Shanghai"));
            return ZonedDateTime.parse(dateString, formatter);
        } else {
            return null;
        }
    }

    public static Date zonedDateTimeToDate(ZonedDateTime zonedDateTime) {
        return ObjectUtils.isNotEmpty(zonedDateTime) ? Date.from(zonedDateTime.toInstant()) : new Date();
    }

    public static ZonedDateTime dateToZonedDateTime(Date date) {
        return ObjectUtils.isNotEmpty(date) ? ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()) : ZonedDateTime.now();
    }

    public static LocalDateTime toLocalDateTime(Date creationDate) {
        return ObjectUtils.isNotEmpty(creationDate) ? LocalDateTime.ofInstant(creationDate.toInstant(), ZoneId.systemDefault()) : LocalDateTime.now();
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        return ObjectUtils.isNotEmpty(instant) ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : LocalDateTime.now();
    }
}
