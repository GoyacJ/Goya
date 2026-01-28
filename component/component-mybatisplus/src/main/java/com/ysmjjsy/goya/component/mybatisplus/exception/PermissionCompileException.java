package com.ysmjjsy.goya.component.mybatisplus.exception;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.mybatisplus.permission.compiler.Explain;
import lombok.Getter;

import java.io.Serial;

/**
 * <p>权限规则编译异常</p>
 * <p>
 * 编译器在遇到不满足安全约束、变量类型不匹配、字段不在白名单等情况时抛出。
 * 上层应根据 failClosed 策略决定：
 * <ul>
 *   <li>failClosed=true：追加 1=0（拒绝访问）</li>
 *   <li>failClosed=false：忽略该规则并记录告警</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 22:43
 */
@Getter
public class PermissionCompileException extends GoyaException {
    @Serial
    private static final long serialVersionUID = -5112863263715376943L;

    private final Explain explain;

    public PermissionCompileException(String userMessage, Explain explain) {
        super(userMessage);
        this.explain = explain;
    }

    public PermissionCompileException(String userMessage, Throwable cause, Explain explain) {
        super(userMessage, cause);
        this.explain = explain;
    }

    public PermissionCompileException(Throwable cause, Explain explain) {
        super(cause);
        this.explain = explain;
    }

    public PermissionCompileException(ErrorCode errorCode, String userMessage, Explain explain) {
        super(errorCode, userMessage);
        this.explain = explain;
    }

    public PermissionCompileException(ErrorCode errorCode, Explain explain) {
        super(errorCode);
        this.explain = explain;
    }

    public PermissionCompileException(ErrorCode errorCode, Throwable cause, Explain explain) {
        super(errorCode, cause);
        this.explain = explain;
    }
}
