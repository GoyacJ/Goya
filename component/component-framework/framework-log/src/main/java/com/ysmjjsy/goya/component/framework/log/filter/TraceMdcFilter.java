package com.ysmjjsy.goya.component.framework.log.filter;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 22:26
 */
@RequiredArgsConstructor
public class TraceMdcFilter extends OncePerRequestFilter {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final Tracer tracer;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 只恢复/清理我们写的几个 key，避免干扰 tracing 自己的 MDC 管理
        String prevTenant = MDC.get(DefaultConst.X_TENANT_ID);
        String prevUser = MDC.get(DefaultConst.X_USER_ID);
        String prevLocale = MDC.get(DefaultConst.LOCALE);
        String prevClientTrace = MDC.get(DefaultConst.CLIENT_TRACE_ID);

        List<BaggageInScope> baggageScopes = new ArrayList<>(3);

        String tenantId = headerOrNull(request, DefaultConst.X_TENANT_ID);
        String userId = headerOrNull(request, DefaultConst.X_USER_ID);
        String localeTag = resolveLocaleTag();

        try {
            putOrClear(DefaultConst.X_TENANT_ID, tenantId);
            putOrClear(DefaultConst.X_USER_ID, userId);
            putOrClear(DefaultConst.LOCALE, localeTag);

            String client = headerOrNull(request, DefaultConst.CLIENT_TRACE_ID);
            putOrClear(DefaultConst.CLIENT_TRACE_ID, client);

            if (tracer != null) {
                // 通过 baggage 传播（是否进入 MDC 由 management.tracing.baggage.correlation.fields 控制）
                // [oai_citation:1‡Home](https://docs.spring.io/spring-boot/reference/actuator/tracing.html)
                addBaggageScope(baggageScopes, "tenantId", tenantId);
                addBaggageScope(baggageScopes, "userId", userId);
                addBaggageScope(baggageScopes, "locale", localeTag);
            }

            String traceId = currentTraceId();
            if (StringUtils.hasText(traceId)) {
                response.setHeader(DefaultConst.X_TRACE_ID, traceId);
            }

            filterChain.doFilter(request, response);
        } finally {
            closeQuietly(baggageScopes);
            restoreOrClear(DefaultConst.X_TENANT_ID, prevTenant);
            restoreOrClear(DefaultConst.X_USER_ID, prevUser);
            restoreOrClear(DefaultConst.LOCALE, prevLocale);
            restoreOrClear(DefaultConst.CLIENT_TRACE_ID, prevClientTrace);
        }
    }
    private void addBaggageScope(List<BaggageInScope> scopes, String name, String value) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(value) || tracer == null) {
            return;
        }
        scopes.add(tracer.createBaggageInScope(name, value));
    }

    private void closeQuietly(List<BaggageInScope> scopes) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            try {
                scopes.get(i).close();
            } catch (Exception ignore) {
                // ignore
            }
        }
    }

    private String currentTraceId() {
        // 优先从 Tracer 获取（最准确）
        if (tracer != null) {
            Span span = tracer.currentSpan();
            if (span != null && span.context() != null) {
                String id = span.context().traceId();
                if (StringUtils.hasText(id)) {
                    return id;
                }
            }
        }
        // 兜底从 MDC 取
        return MDC.get(DefaultConst.X_TRACE_ID);
    }

    private String headerOrNull(HttpServletRequest request, String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        String v = request.getHeader(name);
        return StringUtils.hasText(v) ? v.trim() : null;
    }

    private String resolveLocaleTag() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale == null) {
            return null;
        }
        String tag = locale.toLanguageTag();
        return StringUtils.hasText(tag) ? tag : null;
    }

    private void putOrClear(String key, String value) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        if (StringUtils.hasText(value)) {
            MDC.put(key, value);
        } else {
            MDC.remove(key);
        }
    }

    private void restoreOrClear(String key, String prev) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        if (StringUtils.hasText(prev)) {
            MDC.put(key, prev);
        } else {
            MDC.remove(key);
        }
    }
}
