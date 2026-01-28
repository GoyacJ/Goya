package com.ysmjjsy.goya.component.mybatisplus.permission.resource;

import java.util.Locale;
import java.util.Objects;

/**
 * <p>带“约定推导兜底”的 ResourceRegistry 包装器</p>
 * <p>
 * 规则：
 * <ol>
 *   <li>优先使用委托 registry 的 resolveResource（msId/table 映射）</li>
 *   <li>若仍返回 null，则用表名约定推导 resource</li>
 * </ol>
 * @author goya
 * @since 2026/1/29 00:33
 */
public final class ConventionFallbackResourceRegistry implements ResourceRegistry {

    private final ResourceRegistry delegate;
    private final ConventionResourceResolver resolver;

    public ConventionFallbackResourceRegistry(ResourceRegistry delegate, ConventionResourceResolver resolver) {
        this.delegate = Objects.requireNonNull(delegate, "delegate 不能为空");
        this.resolver = Objects.requireNonNull(resolver, "resolver 不能为空");
    }

    @Override
    public String resolveResource(String tableName, String mappedStatementId) {
        String r = delegate.resolveResource(tableName, mappedStatementId);
        if (r != null) {
            return r;
        }
        if (tableName == null || tableName.isBlank()) {
            return null;
        }
        return resolver.resolve(tableName);
    }

    @Override
    public ColumnRef resolveColumn(String resource, String fieldKey) {
        return delegate.resolveColumn(resource, fieldKey);
    }

    /**
     * 表名约定推导器。
     */
    public interface ConventionResourceResolver {

        /**
         * 根据表名推导 resource。
         *
         * @param tableName 表名
         * @return resource；无法推导返回 null
         */
        String resolve(String tableName);
    }

    /**
     * 默认约定推导器：
     * <ul>
     *   <li>t_order → ORDER</li>
     *   <li>order → ORDER</li>
     * </ul>
     */
    public static final class DefaultConventionResourceResolver implements ConventionResourceResolver {

        @Override
        public String resolve(String tableName) {
            String t = tableName.trim().toLowerCase(Locale.ROOT);
            if (t.startsWith("t_") && t.length() > 2) {
                t = t.substring(2);
            }
            if (t.isBlank()) {
                return null;
            }
            return t.toUpperCase(Locale.ROOT);
        }
    }
}