package com.ysmjjsy.goya.component.bus.version;

import com.ysmjjsy.goya.component.bus.definition.IEvent;

/**
 * <p>版本兼容性检查器接口</p>
 * <p>用于检查事件版本与监听器版本的兼容性</p>
 * <p>默认实现只记录日志，不进行实际检查</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * public class CustomVersionCompatibilityChecker implements IVersionCompatibilityChecker {
 *     @Override
 *     public boolean isCompatible(String eventVersion, String listenerVersion) {
 *         // 自定义版本兼容性检查逻辑
 *         return eventVersion.equals(listenerVersion) || 
 *                isBackwardCompatible(eventVersion, listenerVersion);
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/27
 */
public interface IVersionCompatibilityChecker {

    /**
     * 检查事件版本与监听器版本是否兼容
     * <p>默认实现：只记录日志，不进行实际检查（总是返回 true）</p>
     * <p>子类可以实现自定义的版本兼容性检查逻辑</p>
     *
     * @param eventVersion    事件版本
     * @param listenerVersion 监听器支持的版本（可以从注解或配置中获取）
     * @return true 如果兼容，false 如果不兼容
     */
    default boolean isCompatible(String eventVersion, String listenerVersion) {
        // 默认实现：总是返回 true，不进行实际检查
        // 子类可以实现自定义逻辑
        return true;
    }

    /**
     * 检查事件版本与监听器版本是否兼容（使用事件对象）
     * <p>默认实现：调用 {@link #isCompatible(String, String)}</p>
     *
     * @param event           事件对象
     * @param listenerVersion 监听器支持的版本
     * @return true 如果兼容，false 如果不兼容
     */
    default boolean isCompatible(IEvent event, String listenerVersion) {
        if (event == null) {
            return false;
        }
        return isCompatible(event.eventVersion(), listenerVersion);
    }
}

