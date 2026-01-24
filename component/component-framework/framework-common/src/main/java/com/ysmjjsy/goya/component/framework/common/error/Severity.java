package com.ysmjjsy.goya.component.framework.common.error;

/**
 * <p>错误严重级别，用于告警、审计与故障分级。</p>
 * <p>严重级别与 HTTP 状态码无关；它更偏向“对系统稳定性/业务影响程度”的度量。</p>
 *
 * <h2>使用建议</h2>
 * <ul>
 *   <li>{@link #INFO}：可忽略或仅用于统计（例如幂等重复请求被拒绝但不算异常）。</li>
 *   <li>{@link #WARN}：预期内问题或用户行为导致的失败（例如参数错误、权限不足）。</li>
 *   <li>{@link #ERROR}：需要排查的错误（例如远程调用失败、基础设施抖动）。</li>
 *   <li>{@link #FATAL}：严重故障，可能影响大面积业务（例如关键依赖不可用、数据损坏风险）。</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/24 13:21
 */
public enum Severity {

    /**
     * 信息级，通常不触发告警。
     */
    INFO,

    /**
     * 警告级，可能需要关注但通常无需立即处理。
     */
    WARN,

    /**
     * 错误级，通常需要排查与修复。
     */
    ERROR,

    /**
     * 致命级，通常需要立即响应，可能触发高优先级告警。
     */
    FATAL
}
