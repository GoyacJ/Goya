package com.ysmjjsy.goya.component.framework.security.decision;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * <p>列级约束</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class ColumnConstraint implements Serializable {
    @Serial
    private static final long serialVersionUID = 5635998036551132574L;

    private Set<String> allowColumns;
    private Set<String> denyColumns;
}
