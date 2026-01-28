package com.ysmjjsy.goya.component.mybatisplus.context;

/**
 * <p>访问者画像上下文（线程级）</p>
 * <p>
 * 统一管理当前线程的 subjectId/userId/attributes，用于：
 * <ul>
 *   <li>动态权限规则加载（subjectId 作为缓存主键之一）</li>
 *   <li>权限规则变量替换（从 attributes 取值并做类型校验）</li>
 *   <li>审计字段填充（userId）</li>
 * </ul>
 *
 * <p><b>约束：</b>
 * <ul>
 *   <li>AccessContext 建议在请求进入时建立，在 finally 清理</li>
 *   <li>缺失时是否 fail-closed 由权限开关策略决定</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 22:33
 */
public class AccessContext {

    private static final ThreadLocal<AccessContextValue> HOLDER = new ThreadLocal<>();

    private AccessContext() {
    }

    /**
     * 写入当前线程访问者画像上下文。
     *
     * @param value 上下文值
     */
    public static void set(AccessContextValue value) {
        HOLDER.set(value);
    }

    /**
     * 获取当前线程访问者画像上下文。
     *
     * @return 上下文值；若未设置则返回 null
     */
    public static AccessContextValue get() {
        return HOLDER.get();
    }

    /**
     * 清理当前线程访问者画像上下文。
     * <p>
     * 必须在 finally 调用，防止线程复用污染。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
