package com.ysmjjsy.goya.component.log.event;

import com.ysmjjsy.goya.component.bus.core.event.IEvent;
import com.ysmjjsy.goya.component.framework.enums.StateEnum;
import com.ysmjjsy.goya.component.log.enums.OperatorTypeEnum;

import java.time.LocalDateTime;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/8 21:12
 */
public record OperatorLogEvent(
        /*
         * 日志主键
         */
        String logId,

        /*
         * 租户ID
         */
        String tenantId,

        /*
         * 操作模块
         */
        String title,

        /*
         * 请求方法
         */
        String method,

        /*
         * 请求方式
         */
        String requestMethod,

        /*
         * 操作类别（0其它 1后台用户 2手机端用户）
         */
        OperatorTypeEnum operatorType,

        /*
         * 操作人员
         */
        String operId,

        /*
         * 请求url
         */
        String operUrl,

        /*
         * 操作地址
         */
        String operIp,

        /*
         * 请求参数
         */
        String operParam,

        /*
         * 返回参数
         */
        String jsonResult,

        /*
         * 操作状态（0正常 1异常）
         */
        StateEnum status,

        /*
         * 错误消息
         */
        String errorMsg,

        /*
         * 操作时间
         */
        LocalDateTime operTime,

        /*
         * 消耗时间
         */
        Long costTime
) implements IEvent {
}
