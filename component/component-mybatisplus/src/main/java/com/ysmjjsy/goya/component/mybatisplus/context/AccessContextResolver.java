package com.ysmjjsy.goya.component.mybatisplus.context;

/**
 * <p>AccessContext 解析器</p>
 * <p>
 * 用于在请求/调用进入时构建 {@link AccessContextValue}（subjectId/userId/attributes）。
 *
 * <p><b>默认实现：</b>
 * 返回最小空上下文（subjectId 为空）。当权限 failClosed=true 时会导致权限追加 1=0（符合安全默认）。
 *
 * <p><b>推荐集成：</b>
 * 在 classpath 存在 Spring Security 时，业务侧提供一个 @Bean 覆盖该接口实现，
 * 从 Authentication / Principal 中解析 userId/roles/deptIds 等并填充 attributes。
 *
 * @author goya
 * @since 2026/1/28 23:34
 */
public interface AccessContextResolver {

    /**
     * 解析当前访问上下文。
     *
     * @return AccessContextValue；允许返回 null（表示无法解析）
     */
    AccessContextValue resolve();
}
