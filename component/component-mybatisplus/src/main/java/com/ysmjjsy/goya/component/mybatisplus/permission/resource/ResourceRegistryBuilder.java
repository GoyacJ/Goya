package com.ysmjjsy.goya.component.mybatisplus.permission.resource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * <p>资源注册表构建器</p>
 * <p>
 * 用于构建全局唯一的 {@link ResourceRegistry}：
 * <ul>
 *   <li>msId → resource（精确绑定，优先级最高）</li>
 *   <li>table → resource（兜底映射）</li>
 *   <li>(resource, fieldKey) → ColumnRef（字段白名单，安全核心）</li>
 * </ul>
 *
 * <h2>推荐使用方式</h2>
 * 以“资源”为单位声明字段白名单：
 * <pre>{@code
 * @Bean
 * public ResourceRegistry resourceRegistry() {
 *   return ResourceRegistryBuilder.create()
 *     .enableConventionResource(true)
 *     .resource("ORDER", r -> r
 *         .table("t_order")                       // 可选：主表映射兜底
 *         .field("deptId", "t_order", "dept_id")  // 白名单字段
 *         .field("ownerId", "t_order", "owner_id")
 *         .ms("com.xx.mapper.OrderMapper.selectPage") // 可选：精确 msId 映射
 *     )
 *     .build();
 * }
 * }</pre>
 *
 * <h2>安全与校验</h2>
 * <ul>
 *   <li>字段映射（resource+fieldKey→column）必须显式注册，否则运行时拒绝</li>
 *   <li>列名/表名会被 {@link ColumnNameValidator} 强校验，防注入</li>
 *   <li>默认开启 fail-fast：启动时检查重复 key、非法列名</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 23:08
 */
public class ResourceRegistryBuilder {

    private final Map<String, String> msIdToResource = new HashMap<>();
    private final Map<String, String> tableToResource = new HashMap<>();
    private final Map<DefaultResourceRegistry.ResourceFieldKey, ColumnRef> fieldMapping = new HashMap<>();

    private ColumnNameValidator validator = new ColumnNameValidator();

    /**
     * 是否启用“表名约定推导资源”。
     * <p>
     * 开启后，当 msId/table 映射都未命中时，可通过表名推导 resource：
     * <ul>
     *   <li>t_order → ORDER</li>
     *   <li>order → ORDER</li>
     * </ul>
     *
     * <p>
     * 注意：约定推导仅用于 resolveResource；字段白名单仍必须显式注册。
     */
    private boolean conventionResourceEnabled = false;

    /**
     * 推导规则：若表名以 t_ 开头，则剥离前缀，否则直接使用表名；
     * 最终转为大写作为 resource。
     */
    private ConventionFallbackResourceRegistry.ConventionResourceResolver conventionResolver = new ConventionFallbackResourceRegistry.DefaultConventionResourceResolver();

    /**
     * 是否在 build 时执行 fail-fast 校验。
     */
    private boolean failFast = true;

    private ResourceRegistryBuilder() {
    }

    /**
     * 创建构建器。
     *
     * @return builder
     */
    public static ResourceRegistryBuilder create() {
        return new ResourceRegistryBuilder();
    }

    /**
     * 设置列名校验器。
     *
     * @param validator validator
     * @return this
     */
    public ResourceRegistryBuilder validator(ColumnNameValidator validator) {
        if (validator != null) {
            this.validator = validator;
        }
        return this;
    }

    /**
     * 启用/禁用“表名约定推导资源”。
     *
     * @param enabled 是否启用
     * @return this
     */
    public ResourceRegistryBuilder enableConventionResource(boolean enabled) {
        this.conventionResourceEnabled = enabled;
        return this;
    }

    /**
     * 自定义约定推导器。
     *
     * @param resolver resolver
     * @return this
     */
    public ResourceRegistryBuilder conventionResolver(ConventionFallbackResourceRegistry.ConventionResourceResolver resolver) {
        if (resolver != null) {
            this.conventionResolver = resolver;
        }
        return this;
    }

    /**
     * 是否启用 fail-fast 校验（默认 true）。
     *
     * @param failFast 是否 fail-fast
     * @return this
     */
    public ResourceRegistryBuilder failFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    /**
     * 以“资源”为单位注册映射。
     *
     * @param resource 资源名（建议全大写，如 ORDER）
     * @param consumer 资源字段注册器
     * @return this
     */
    public ResourceRegistryBuilder resource(String resource, Consumer<ResourceFieldsBuilder> consumer) {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("resource 不能为空");
        }
        Objects.requireNonNull(consumer, "consumer 不能为空");
        ResourceFieldsBuilder rb = new ResourceFieldsBuilder(resource.trim(), this);
        consumer.accept(rb);
        return this;
    }

    /**
     * 直接注册 msId → resource 映射（精确绑定）。
     *
     * @param mappedStatementId msId
     * @param resource resource
     * @return this
     */
    public ResourceRegistryBuilder mapStatement(String mappedStatementId, String resource) {
        requireNonBlank(mappedStatementId, "mappedStatementId 不能为空");
        requireNonBlank(resource, "resource 不能为空");
        String prev = msIdToResource.put(mappedStatementId.trim(), resource.trim());
        if (prev != null && !prev.equals(resource.trim())) {
            throw new IllegalStateException("重复注册 msId 映射且资源不一致: msId=" + mappedStatementId + " prev=" + prev + " now=" + resource);
        }
        return this;
    }

    /**
     * 直接注册 table → resource 映射（兜底）。
     *
     * @param tableName 表名
     * @param resource resource
     * @return this
     */
    public ResourceRegistryBuilder mapTable(String tableName, String resource) {
        requireNonBlank(tableName, "tableName 不能为空");
        requireNonBlank(resource, "resource 不能为空");
        String key = normalizeTable(tableName);
        String prev = tableToResource.put(key, resource.trim());
        if (prev != null && !prev.equals(resource.trim())) {
            throw new IllegalStateException("重复注册 table 映射且资源不一致: table=" + tableName + " prev=" + prev + " now=" + resource);
        }
        return this;
    }

    /**
     * 注册字段白名单映射：resource + fieldKey → ColumnRef。
     *
     * @param resource resource
     * @param fieldKey fieldKey（规则侧使用）
     * @param table 表名（可为空；建议填主表名，便于审计与跨表限制）
     * @param column 列名
     * @return this
     */
    public ResourceRegistryBuilder mapField(String resource, String fieldKey, String table, String column) {
        requireNonBlank(resource, "resource 不能为空");
        requireNonBlank(fieldKey, "fieldKey 不能为空");
        requireNonBlank(column, "column 不能为空");

        ColumnRef ref = new ColumnRef(table, column);
        DefaultResourceRegistry.ResourceFieldKey key = new DefaultResourceRegistry.ResourceFieldKey(resource.trim(), fieldKey.trim());

        ColumnRef prev = fieldMapping.put(key, ref);
        if (prev != null && !(Objects.equals(prev.table(), ref.table()) && Objects.equals(prev.column(), ref.column()))) {
            throw new IllegalStateException("重复注册字段白名单且列引用不一致: key=" + key + " prev=" + prev + " now=" + ref);
        }
        return this;
    }

    /**
     * 构建 ResourceRegistry。
     *
     * @return ResourceRegistry
     */
    public ResourceRegistry build() {
        if (failFast) {
            validateAll();
        }

        DefaultResourceRegistry base = new DefaultResourceRegistry(
                msIdToResource,
                tableToResource,
                fieldMapping,
                validator
        );

        if (!conventionResourceEnabled) {
            return base;
        }
        return new ConventionFallbackResourceRegistry(base, conventionResolver);
    }

    private void validateAll() {
        // 1) 校验 tableToResource
        for (String table : tableToResource.keySet()) {
            validator.validateTable(table);
        }

        // 2) 校验 fieldMapping 的 table/column
        for (Map.Entry<DefaultResourceRegistry.ResourceFieldKey, ColumnRef> e : fieldMapping.entrySet()) {
            ColumnRef ref = e.getValue();
            validator.validateTable(ref.table());
            validator.validate(ref.column());
        }
    }

    private static void requireNonBlank(String s, String msg) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(msg);
        }
    }

    private static String normalizeTable(String tableName) {
        return tableName.trim().toLowerCase(Locale.ROOT);
    }
}