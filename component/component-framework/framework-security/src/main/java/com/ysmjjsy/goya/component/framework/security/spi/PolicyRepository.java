package com.ysmjjsy.goya.component.framework.security.spi;

import com.ysmjjsy.goya.component.framework.security.domain.Policy;
import com.ysmjjsy.goya.component.framework.security.domain.PolicyQuery;
import org.jspecify.annotations.NonNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * <p>策略仓储。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
public interface PolicyRepository {

    /**
     * 查询生效的策略列表。
     *
     * @param query 查询条件
     * @return 生效策略列表
     */
    List<Policy> findEffectivePolicies(@Validated @NonNull PolicyQuery query);
}
