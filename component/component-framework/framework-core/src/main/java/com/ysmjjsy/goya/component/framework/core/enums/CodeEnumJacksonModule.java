package com.ysmjjsy.goya.component.framework.core.enums;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * <p>CodeEnum Jackson 模块：注册通用反序列化器。</p>
 *
 * @author goya
 * @since 2026/1/24 15:45
 */
public class CodeEnumJacksonModule {

    private CodeEnumJacksonModule() {
    }

    /**
     * 创建 Jackson Module。
     *
     * @return Module
     */
    public static Module create() {
        SimpleModule module = new SimpleModule("Goya-CodeEnum-Module");
        // 关键：对 Enum 类型统一注册反序列化器（内部会判断是否是 CodeEnum）
        module.addDeserializer(Enum.class, new CodeEnumJsonDeserializer());
        return module;
    }
}
