package com.ysmjjsy.goya.component.mybatisplus.permission.compiler;

import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>规则变量解析器</p>
 * <p>
 * 支持：
 * <ul>
 *   <li>${userId}：解析为 AccessContextValue.userId()</li>
 *   <li>${xxx}：解析为 AccessContextValue.attributes().get("xxx")</li>
 * </ul>
 *
 * <p><b>扁平化规则：</b>
 * <ul>
 *   <li>变量解析结果若为 Collection/数组，会被展开为多个值</li>
 *   <li>Map 不允许作为谓词值（结构不稳定），会记录错误并视为失败</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 22:48
 */
public final class VariableResolver {

    private VariableResolver() {
    }

    /**
     * 解析并扁平化谓词值列表。
     *
     * @param rawValues 原始值列表
     * @param access 访问上下文
     * @param explain Explain
     * @param contextMsg 上下文信息（用于 Explain 记录）
     * @return 扁平化后的值列表（元素为 String/Number/Boolean/Date/Temporal 等基础类型）
     */
    public static List<Object> resolveValues(List<? extends Serializable> rawValues,
                                             AccessContextValue access,
                                             Explain explain,
                                             String contextMsg) {

        if (rawValues == null || rawValues.isEmpty()) {
            return List.of();
        }

        List<Object> out = new ArrayList<>();
        for (Serializable v : rawValues) {
            Object resolved = resolveOne(v, access, explain, contextMsg);
            if (resolved == null) {
                continue;
            }
            flatten(out, resolved, explain, contextMsg);
        }
        return out;
    }

    private static Object resolveOne(Object v,
                                     AccessContextValue access,
                                     Explain explain,
                                     String contextMsg) {

        if (v == null) {
            return null;
        }

        if (v instanceof String s) {
            String trimmed = s.trim();
            if (isVar(trimmed)) {
                String key = trimmed.substring(2, trimmed.length() - 1).trim();
                if ("userId".equals(key)) {
                    return access.userId();
                }
                Object attr = access.attributes().get(key);
                if (attr == null) {
                    explain.error("变量不存在：" + key + " @" + contextMsg);
                    return null;
                }
                return attr;
            }
            return s;
        }

        return v;
    }

    private static void flatten(List<Object> out,
                                Object v,
                                Explain explain,
                                String contextMsg) {

        if (v == null) {
            return;
        }
        if (v instanceof Map<?, ?>) {
            explain.error("不允许使用 Map 作为谓词值 @" + contextMsg);
            return;
        }
        if (v instanceof Collection<?> c) {
            for (Object e : c) {
                flatten(out, e, explain, contextMsg);
            }
            return;
        }
        if (v.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(v);
            for (int i = 0; i < len; i++) {
                Object e = java.lang.reflect.Array.get(v, i);
                flatten(out, e, explain, contextMsg);
            }
            return;
        }
        out.add(v);
    }

    private static boolean isVar(String s) {
        return s.startsWith("${") && s.endsWith("}") && s.length() >= 4;
    }
}