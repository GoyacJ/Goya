package com.ysmjjsy.goya.component.bus.stream.processor;

/**
 * <p>事件拦截器接口</p>
 * <p>用于在事件处理 Pipeline 中执行特定逻辑</p>
 * <p>拦截器按顺序执行，可以修改 EventContext 或中止处理</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * @Component
 * public class CustomInterceptor implements IEventInterceptor {
 *     @Override
 *     public void intercept(EventContext context) {
 *         if (context.isAborted()) {
 *             return; // 已中止，跳过
 *         }
 *         // 处理逻辑
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *         return 100; // 执行顺序
 *     }
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/21
 */
public interface IEventInterceptor {

    /**
     * 拦截事件处理
     * <p>在 Pipeline 中执行特定逻辑，可以修改 EventContext 或中止处理</p>
     *
     * @param context 事件处理上下文
     */
    void intercept(EventContext context);

    /**
     * 获取拦截器执行顺序
     * <p>数值越小，越先执行</p>
     * <p>默认顺序：</p>
     * <ul>
     *   <li>DeserializeInterceptor: 100</li>
     *   <li>IdempotencyInterceptor: 200</li>
     *   <li>RouteInterceptor: 300</li>
     *   <li>InvokeInterceptor: 400</li>
     * </ul>
     *
     * @return 执行顺序
     */
    default int getOrder() {
        return 500;
    }
}

