package com.ysmjjsy.goya.component.framework.common.error;

import java.util.Collection;

/**
 * <p>错误码目录（注册式治理入口）。</p>
 *
 * <p>推荐每个模块提供一个目录实现，集中列出该模块的所有 {@link ErrorCode}，
 * 并在 Spring 环境中将该目录注册为 Bean，供 framework-core 的治理工具统一校验。</p>
 *
 * <h2>为何采用注册式而非 classpath 扫描？</h2>
 * <ul>
 *   <li>无反射黑魔法，启动性能更稳定。</li>
 *   <li>可控、显式、可审计，便于治理。</li>
 *   <li>避免因枚举未被加载导致扫描遗漏。</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/24 14:20
 */
public interface ErrorCodeCatalog {

    /**
     * 返回该目录包含的所有错误码集合。
     *
     * @return 错误码集合（不能为空）
     */
    Collection<? extends ErrorCode> codes();
}
