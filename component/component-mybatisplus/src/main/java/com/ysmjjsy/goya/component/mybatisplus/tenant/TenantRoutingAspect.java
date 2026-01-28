package com.ysmjjsy.goya.component.mybatisplus.tenant;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.context.*;
import com.ysmjjsy.goya.component.mybatisplus.permission.cache.PermissionRequestCache;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>租户路由切面（非 Web 场景可选）</p>
 * 用于在没有 Servlet 过滤器链的服务中（例如定时任务、消息消费、纯 RPC 服务）完成与
 * {@link TenantRoutingFilter} 相同的职责：在事务开始之前写入 dsKey，并建立上下文。
 *
 * <p><b>使用方式：</b>
 * 上层项目可通过切点表达式约束其生效范围，例如只对某些入口方法生效。
 * @author goya
 * @since 2026/1/28 21:57
 */
@Aspect
@RequiredArgsConstructor
public class TenantRoutingAspect {

    private final TenantResolver tenantResolver;
    private final TenantProfileStore tenantProfileStore;
    private final AccessContextResolver accessContextResolver;
    private final GoyaMybatisPlusProperties props;

    /**
     * 围绕 @Transactional 的方法执行租户路由注入。
     *
     * @param pjp 连接点
     * @return 方法返回值
     * @throws Throwable 原方法异常
     */
    @Around("@annotation(transactional)")
    public Object aroundTransactional(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
        try {
            // 1) 解析 tenantId
            String tenantId = tenantResolver.resolveTenantId();

            // tenant 缺失处理
            if (isBlank(tenantId)) {
                if (props.tenant().requireTenant()) {
                    throw new IllegalStateException("缺少租户信息（requireTenant=true），无法执行事务方法");
                }
                // requireTenant=false：允许继续
                setAccessContextIfPresent();
                return pjp.proceed();
            }

            // 2) 加载租户画像（已含缓存+version）
            TenantProfile profile = tenantProfileStore.load(tenantId);
            if (profile == null) {
                if (props.tenant().requireTenant()) {
                    throw new IllegalStateException("租户未配置画像（TenantProfile 不存在），tenantId=" + tenantId);
                }
                // requireTenant=false：允许继续
                setAccessContextIfPresent();
                return pjp.proceed();
            }

            // 3) 建立 TenantContext
            TenantContext.set(new TenantContextValue(tenantId, profile.mode(), profile.dsKey()));

            // 4) push dynamic-datasource（必须在事务开始前）
            if (!isBlank(profile.dsKey())) {
                DynamicDataSourceContextHolder.push(profile.dsKey());
            }

            // 5) 建立 AccessContext
            setAccessContextIfPresent();

            return pjp.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
            TenantContext.clear();
            AccessContext.clear();
            PermissionRequestCache.clear();
        }
    }


    /**
     * 尝试解析并设置 AccessContext（允许解析失败/返回 null）。
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
