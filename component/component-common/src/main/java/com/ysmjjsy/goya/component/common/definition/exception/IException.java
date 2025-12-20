package com.ysmjjsy.goya.component.common.definition.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.pojo.IResponse;
import com.ysmjjsy.goya.component.common.i18n.I18nResolver;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>base exception interface</p>
 *
 * @author goya
 * @since 2025/12/19 22:28
 */
public interface IException extends Serializable {

    /**
     * 获取状态码
     *
     * @return IResponseCode
     */
    IResponseCode getCode();

    /**
     * 转换为响应
     *
     * @return IResponse
     */
    default IResponse toResponses() {
        return new IResponse() {
            @Serial
            private static final long serialVersionUID = 2472722041252817887L;

            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String code() {
                return getCode().getCode();
            }

            @Override
            public String message() {
                return getCode().getDescription();
            }
        };
    }

    /**
     * 格式化消息
     *
     * @param code IResponseCode
     * @param ext  扩展信息
     * @return 格式化后的消息
     */
    static String formatMessage(IResponseCode code, String... ext) {
        if (code == null) {
            return I18nResolver.resolveEnum(ResponseCodeEnum.INTERNAL_SERVER_ERROR);
        }

        // 获取国际化消息
        String message = I18nResolver.resolveEnum(code);

        // 如果没有扩展信息，直接返回消息
        if (ext == null || ext.length == 0) {
            return message;
        }

        // 过滤空字符串，避免多余空格
        String extStr = Arrays.stream(ext)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));

        return extStr.isEmpty() ? message : String.format("[%s] %s", message, extStr);
    }
}
