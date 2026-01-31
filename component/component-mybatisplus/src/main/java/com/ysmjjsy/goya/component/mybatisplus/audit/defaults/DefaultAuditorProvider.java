package com.ysmjjsy.goya.component.mybatisplus.audit.defaults;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.mybatisplus.audit.AuditorProvider;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContext;

/**
 * <p>默认审计人提供者</p>
 *
 * <p>优先从 AccessContext 获取 userId，缺失则返回默认用户。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public class DefaultAuditorProvider implements AuditorProvider {

    /**
     * 获取当前审计人。
     *
     * @return 审计人
     */
    @Override
    public String currentAuditor() {
        String userId = AccessContext.get().userId();
        return userId == null || userId.isBlank() ? DefaultConst.DEFAULT_USER : userId;
    }
}
