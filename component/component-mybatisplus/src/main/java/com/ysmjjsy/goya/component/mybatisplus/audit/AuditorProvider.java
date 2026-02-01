package com.ysmjjsy.goya.component.mybatisplus.audit;

/**
 * <p>审计人提供者</p>
 *
 * <p>用于填充 created_by / updated_by 字段。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public interface AuditorProvider {

    /**
     * 获取当前操作人标识。
     *
     * @return 操作人
     */
    String currentAuditor();
}
