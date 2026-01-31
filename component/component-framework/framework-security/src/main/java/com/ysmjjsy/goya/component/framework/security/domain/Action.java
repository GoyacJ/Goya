package com.ysmjjsy.goya.component.framework.security.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>操作定义。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class Action implements Serializable {

    @Serial
    private static final long serialVersionUID = -4152778245826123217L;

    /**
     * 操作编码，例如 QUERY/CREATE/UPDATE/DELETE。
     */
    private String code;

    /**
     * 显示名称。
     */
    private String name;
}
