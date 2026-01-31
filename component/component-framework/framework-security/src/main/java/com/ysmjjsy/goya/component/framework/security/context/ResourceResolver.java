package com.ysmjjsy.goya.component.framework.security.context;

import com.ysmjjsy.goya.component.framework.security.domain.Resource;
import org.jspecify.annotations.NonNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * <p>资源解析器。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
public interface ResourceResolver {

    /**
     * 解析资源信息。
     *
     * @param context 资源上下文
     * @return 解析后的资源
     */
    Resource resolve(@Validated @NonNull ResourceContext context);

    /**
     * 解析资源的子级列表。
     *
     * @param resource 资源
     * @return 子级资源列表
     */
    List<Resource> resolveChildren(@NonNull Resource resource);
}
