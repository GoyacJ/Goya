package com.ysmjjsy.goya.component.common.definition.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.pojo.IResponse;

import java.io.Serial;
import java.io.Serializable;

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
            return ResponseCodeEnum.INTERNAL_SERVER_ERROR.getDescription();
        }
        String name = code.getI18nKey();

        if (ext == null || ext.length == 0) {
            return name;
        }

        // 拼接 ext 为空格分隔的字符串
        String extStr = String.join(" ", ext);
        return String.format("[%s]: %s", name, extStr);
    }
}
