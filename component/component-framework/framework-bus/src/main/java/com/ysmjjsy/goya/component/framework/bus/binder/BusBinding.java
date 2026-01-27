package com.ysmjjsy.goya.component.framework.bus.binder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>统一 binding 模型</p>
 *
 * @param binder optional preferred binder
 * @author goya
 * @since 2026/1/26 23:43
 */
public record BusBinding(
        String name,
        String destination,
        String group,
        String binder,
        Map<String, String> binderBindingNames
) {
    public BusBinding {
        if (binderBindingNames == null) {
            binderBindingNames = new LinkedHashMap<>();
        }
    }

    /**
     * 针对某个 binder 生成“实际发送目标”：
     * - 默认 destination 不变
     * - 如果 binderBindingNames 中存在映射，则使用映射值（常用于 stream 的 binding name）
     */
    public BusBinding forBinder(String binderName) {
        if (binderName == null || binderName.isBlank()) {
            return this;
        }
        String mapped = binderBindingNames.get(binderName);
        if (mapped == null || mapped.isBlank()) {
            return this;
        }
        return new BusBinding(name, mapped, group, binder, binderBindingNames);
    }
}