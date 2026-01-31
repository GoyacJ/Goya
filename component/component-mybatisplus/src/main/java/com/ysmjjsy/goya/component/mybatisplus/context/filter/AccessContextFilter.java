package com.ysmjjsy.goya.component.mybatisplus.context.filter;

import com.ysmjjsy.goya.component.mybatisplus.context.AccessContext;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextResolver;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextValue;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <p>访问上下文过滤器</p>
 *
 * <p>当线程上下文为空时，自动解析并写入 AccessContext。</p>
 *
 * @author goya
 * @since 2026/1/31 13:30
 */
@RequiredArgsConstructor
public class AccessContextFilter extends OncePerRequestFilter {

    private final AccessContextResolver resolver;

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        AccessContextValue existing = AccessContext.get();
        if (hasContext(existing)) {
            filterChain.doFilter(request, response);
            return;
        }

        AccessContextValue resolved = resolver.resolve(request);
        boolean applied = hasContext(resolved);
        if (applied) {
            AccessContext.set(resolved);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (applied) {
                AccessContext.clear();
            }
        }
    }

    private boolean hasContext(AccessContextValue value) {
        if (value == null) {
            return false;
        }
        return StringUtils.hasText(value.subjectId())
                || StringUtils.hasText(value.userId())
                || value.subjectType() != null
                || !CollectionUtils.isEmpty(value.attributes());
    }
}
