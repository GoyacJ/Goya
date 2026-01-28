package com.ysmjjsy.goya.component.mybatisplus.tenant;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.context.*;
import com.ysmjjsy.goya.component.mybatisplus.permission.cache.PermissionRequestCache;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <p>租户路由 Filter（Web 场景</p>
 * <p>
 * 该 Filter 是多租户混合模式的“请求入口注入点”，负责在进入业务逻辑与事务之前：
 * <ol>
 *   <li>解析 tenantId（{@link TenantResolver}）</li>
 *   <li>加载租户画像（{@link TenantProfileStore}，内部已包含缓存 + version 快速生效）</li>
 *   <li>根据画像建立 {@link TenantContext}</li>
 *   <li>将 dsKey push 到 dynamic-datasource 上下文（必须早于事务开启）</li>
 *   <li>建立 {@link AccessContext}（用户画像/主体信息）</li>
 * </ol>
 *
 * <h2>失败策略</h2>
 * <ul>
 *   <li>requireTenant=true：tenantId 缺失或 TenantProfile 缺失，直接拒绝（抛异常）</li>
 *   <li>requireTenant=false：允许 tenantId/profile 缺失时继续（不设置 DS/TenantContext）</li>
 * </ul>
 *
 * <h2>finally 清理（强制）</h2>
 * <ul>
 *   <li>{@link DynamicDataSourceContextHolder#poll()}</li>
 *   <li>{@link TenantContext#clear()}</li>
 *   <li>{@link AccessContext#clear()}</li>
 *   <li>{@link PermissionRequestCache#clear()}</li>
 * </ul>
 *
 * <p><b>顺序建议：</b>
 * 以高优先级运行，确保在 Spring 事务拦截器之前完成数据源路由
 * @author goya
 * @since 2026/1/28 21:52
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@RequiredArgsConstructor
public class TenantRoutingFilter extends OncePerRequestFilter {

    private final TenantResolver tenantResolver;
    private final TenantProfileStore tenantProfileStore;
    private final AccessContextResolver accessContextResolver;
    private final GoyaMybatisPlusProperties props;

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1) 解析 tenantId
            String tenantId = tenantResolver.resolveTenantId();

            // tenant 缺失处理
            if (isBlank(tenantId)) {
                if (props.tenant().requireTenant()) {
                    throw new IllegalStateException("缺少租户信息（requireTenant=true），无法处理请求");
                }
                // requireTenant=false：允许继续
                setAccessContextIfPresent();
                filterChain.doFilter(request, response);
                return;
            }

            // 2) 加载租户画像（已含缓存+version）
            TenantProfile profile = tenantProfileStore.load(tenantId);
            if (profile == null) {
                if (props.tenant().requireTenant()) {
                    throw new IllegalStateException("租户未配置画像（TenantProfile 不存在），tenantId=" + tenantId);
                }
                // requireTenant=false：允许继续（不设置 TenantContext/dsKey）
                setAccessContextIfPresent();
                filterChain.doFilter(request, response);
                return;
            }

            // 3) 建立 TenantContext
            TenantContext.set(new TenantContextValue(tenantId, profile.mode(), profile.dsKey()));

            // 4) 动态数据源路由（必须在事务开始前）
            if (!isBlank(profile.dsKey())) {
                DynamicDataSourceContextHolder.push(profile.dsKey());
            }

            // 5) 建立 AccessContext
            setAccessContextIfPresent();

            filterChain.doFilter(request, response);
        } finally {
            // 必须 finally 清理，防止线程复用污染
            DynamicDataSourceContextHolder.poll();
            TenantContext.clear();
            AccessContext.clear();
            PermissionRequestCache.clear();
        }
    }

    /**
     * 尝试解析并设置 AccessContext（允许解析失败/返回 null）。
     * <p>
     * AccessContext 用于权限引擎变量解析、审计字段填充等。
     */
    private void setAccessContextIfPresent() {
        AccessContextValue access = accessContextResolver.resolve();
        if (access != null) {
            AccessContext.set(access);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}