package com.ysmjjsy.goya.component.bus.enums;

/**
 * <p>消息确认模式枚举</p>
 * <p>用于控制消息的确认方式</p>
 *
 * @author goya
 * @since 2025/12/21
 */
public enum AckMode {

    /**
     * 自动确认
     * <p>消息处理成功后自动确认，无需手动调用 Acknowledgment.acknowledge()</p>
     */
    AUTO,

    /**
     * 手动确认
     * <p>需要在消息处理方法中手动调用 Acknowledgment.acknowledge() 确认消息</p>
     * <p>适用于需要精确控制消息确认时机的场景</p>
     */
    MANUAL
}

