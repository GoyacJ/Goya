package com.ysmjjsy.goya.component.mybatisplus.permission.compiler;

import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextValue;
import com.ysmjjsy.goya.component.mybatisplus.permission.model.RuleSet;
import com.ysmjjsy.goya.component.mybatisplus.permission.resource.ResourceRegistry;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/28 22:32
 */
public interface PermissionCompiler {

    /**
     * 编译规则集。
     *
     * @param ruleSet 规则集
     * @param accessContext 访问者上下文
     * @param registry 资源注册表
     * @return 已编译谓词
     */
    CompiledPredicate compile(RuleSet ruleSet,
                              AccessContextValue accessContext,
                              ResourceRegistry registry);
}