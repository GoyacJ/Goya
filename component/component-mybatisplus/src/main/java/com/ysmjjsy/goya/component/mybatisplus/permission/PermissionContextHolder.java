package com.ysmjjsy.goya.component.mybatisplus.permission;

import com.ysmjjsy.goya.component.framework.security.decision.ColumnConstraint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>权限执行上下文（线程级）</p>
 *
 * <p>用于在权限评估与拦截器之间传递动作与列级约束。</p>
 *
 * @author goya
 * @since 2026/1/31
 */
public final class PermissionContextHolder {

    private static final ThreadLocal<String> ACTION = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, ColumnConstraint>> COLUMN_CONSTRAINTS = new ThreadLocal<>();

    private PermissionContextHolder() {
    }

    /**
     * 设置当前动作编码。
     *
     * @param actionCode 动作编码
     */
    public static void setAction(String actionCode) {
        if (actionCode == null || actionCode.isBlank()) {
            return;
        }
        ACTION.set(actionCode.trim());
    }

    /**
     * 获取当前动作编码。
     *
     * @return 动作编码
     */
    public static String getAction() {
        return ACTION.get();
    }

    /**
     * 注册列级约束。
     *
     * @param resourceCode 资源编码（表名）
     * @param constraint   列级约束
     */
    public static void putConstraint(String resourceCode, ColumnConstraint constraint) {
        if (constraint == null || resourceCode == null || resourceCode.isBlank()) {
            return;
        }
        Map<String, ColumnConstraint> map = COLUMN_CONSTRAINTS.get();
        if (map == null) {
            map = new HashMap<>();
            COLUMN_CONSTRAINTS.set(map);
        }
        map.put(resourceCode.trim().toLowerCase(), constraint);
    }

    /**
     * 获取列级约束集合。
     *
     * @return 约束集合
     */
    public static Map<String, ColumnConstraint> getConstraints() {
        Map<String, ColumnConstraint> map = COLUMN_CONSTRAINTS.get();
        return map == null ? Collections.emptyMap() : map;
    }

    /**
     * 清理上下文。
     */
    public static void clear() {
        ACTION.remove();
        COLUMN_CONSTRAINTS.remove();
    }
}
