package com.ysmjjsy.goya.component.mybatisplus.context;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>当前线程访问者画像上下文值对象</p>
 * <p>
 * 用于动态权限规则加载与变量解析（如 ${userId}、${deptIds} 等）。
 *
 * <p><b>字段语义：</b>
 * <ul>
 *   <li>subjectId：授权主体 ID（规则加载缓存主键之一）</li>
 *   <li>userId：当前用户 ID（审计、常用变量）</li>
 *   <li>attributes：属性集合（如 deptIds、roleCodes 等）</li>
 * </ul>
 *
 * <p><b>约束：</b>
 * attributes 的 value 只能是可序列化且可类型校验的结构，例如：
 * String/Number/Boolean/Collection/Map（嵌套也必须可序列化且结构稳定）。
 *
 * @param subjectId 主体 ID
 * @param userId 用户 ID
 * @param attributes 属性集合
 *
 * @author goya
 * @since 2026/1/28 22:33
 */
public record AccessContextValue(
        String subjectId,
        String userId,
        Map<String, Serializable> attributes
) {
}
