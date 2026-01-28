package com.ysmjjsy.goya.component.mybatisplus.exception;

import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;

import java.io.Serial;

/**
 * <p>跨表字段引用异常</p>
 * <p>
 * 当 ColumnRef 显式绑定 table，但当前 SQL 回调的 Table.name 不匹配时抛出。
 * 该异常用于阻止“规则跨表引用”造成隐性越权或不可预测 SQL。
 *
 * <p><b>建议策略：</b>
 * 上层捕获异常后按 failClosed 处理：
 * <ul>
 *   <li>failClosed=true：追加 1=0</li>
 *   <li>failClosed=false：不追加条件并记录告警</li>
 * </ul>
 * @author goya
 * @since 2026/1/28 23:16
 */
public class CrossTableReferenceException extends GoyaException {
    @Serial
    private static final long serialVersionUID = 5213794753409515290L;

    public CrossTableReferenceException(String userMessage) {
        super(userMessage);
    }

    public CrossTableReferenceException(Throwable cause) {
        super(cause);
    }

    public CrossTableReferenceException(String userMessage, Throwable cause) {
        super(userMessage, cause);
    }
}
