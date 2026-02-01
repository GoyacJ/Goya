package com.ysmjjsy.goya.component.mybatisplus.tenant.filter;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContext;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContextValue;
import com.ysmjjsy.goya.component.mybatisplus.tenant.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <p>租户路由过滤器</p>
 *
 * <p>负责解析租户并设置动态数据源上下文。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@Slf4j
@RequiredArgsConstructor
public class GoyaTenantRoutingFilter extends OncePerRequestFilter {

    private final TenantResolver tenantResolver;
    private final TenantProfileStore profileStore;
    private final TenantShardDecider shardDecider;
    private final TenantDataSourceRouter router;
    private final TenantDataSourceRegistrar dataSourceRegistrar;
    private final GoyaMybatisPlusProperties.Tenant options;

    /**
     * 执行租户路由过滤。
     *
     * @param request 请求
     * @param response 响应
     * @param filterChain 过滤链
     * @throws ServletException 异常
     * @throws IOException 异常
     */
    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantId = tenantResolver.resolveTenantId();
        if (!StringUtils.hasText(tenantId)) {
            if (options.requireTenant()) {
                throw new IllegalStateException("TenantId is required but missing.");
            }
            filterChain.doFilter(request, response);
            return;
        }

        TenantProfile profile = profileStore.load(tenantId);
        TenantMode mode = profile != null && profile.mode() != null ? profile.mode() : shardDecider.decide(tenantId);
        boolean tenantLineEnabled = profile == null || profile.tenantLineEnabled();

        String dsKey = null;
        if (profile != null && profile.dataSourceProfile() != null
                && StringUtils.hasText(profile.dataSourceProfile().jdbcUrl())) {
            dsKey = dataSourceRegistrar.register(tenantId, profile.dataSourceProfile(), profile.dsKey());
        }
        if (!StringUtils.hasText(dsKey)) {
            if (profile != null && StringUtils.hasText(profile.dsKey())) {
                dsKey = profile.dsKey();
            } else {
                dsKey = router.route(tenantId, mode);
            }
        }

        TenantContext.set(new TenantContextValue(tenantId, mode, dsKey, tenantLineEnabled));
        if (StringUtils.hasText(dsKey)) {
            DynamicDataSourceContextHolder.push(dsKey);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            DynamicDataSourceContextHolder.poll();
            TenantContext.clear();
        }
    }
}
