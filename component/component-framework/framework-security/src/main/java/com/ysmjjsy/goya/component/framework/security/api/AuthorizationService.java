package com.ysmjjsy.goya.component.framework.security.api;

import com.ysmjjsy.goya.component.framework.security.decision.Decision;
import com.ysmjjsy.goya.component.framework.security.decision.DecisionExplain;
import org.jspecify.annotations.NonNull;
import org.springframework.validation.annotation.Validated;

/**
 * <p>鉴权服务入口。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
public interface AuthorizationService {

    /**
     * 执行鉴权并返回决策结果。
     *
     * @param request 鉴权请求
     * @return 鉴权决策结果
     */
    Decision authorize(@Validated @NonNull AuthorizeRequest request);

    /**
     * 执行鉴权并返回可解释的决策结果。
     *
     * @param request 鉴权请求
     * @return 鉴权决策结果（包含解释信息）
     */
    DecisionExplain authorizeWithExplain(AuthorizeRequest request);
}
