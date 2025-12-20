package com.ysmjjsy.goya.component.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ysmjjsy.goya.component.common.definition.pojo.VO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>结构化错误信息</p>
 * <p>用于响应体中 error 字段，便于客户端区分错误类型与定位问题</p>
 *
 * @author goya
 * @since 2025/12/20 22:09
 */
@Schema(name = "ErrorDetail", description = "结构化错误详情")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
        @Schema(name = "Exception完整信息", type = "string")
        String detail,

        @Schema(name = "额外的错误字段")
        String field,

        @Schema(name = "错误堆栈信息")
        StackTraceElement[] stackTrace
) implements VO {

    /**
     * 创建带字段的错误信息
     */
    public static ErrorDetail ofField(String field, Throwable throwable) {
        return new ErrorDetail(throwable.getMessage(), field, throwable.getStackTrace());
    }

    /**
     * 创建带堆栈信息的错误对象，仅在调试模式下返回堆栈
     */
    public static ErrorDetail withStackTrace(Throwable throwable) {
        return new ErrorDetail(throwable.getMessage(), null, throwable.getStackTrace());
    }
}