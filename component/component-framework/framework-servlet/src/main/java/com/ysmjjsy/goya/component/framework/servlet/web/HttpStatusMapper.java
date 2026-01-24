package com.ysmjjsy.goya.component.framework.servlet.web;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCategory;
import org.springframework.http.HttpStatus;

/**
 * <p>HTTP 状态码映射器</p>
 * <p>该组件负责将 {@link ErrorCategory} 映射为 {@link HttpStatus}，
 * 是 Web（Servlet）适配层能力，禁止下沉到 framework-core。</p>
 *
 * @author goya
 * @since 2026/1/24 13:51
 */
public interface HttpStatusMapper {

    /**
     * 映射 HTTP 状态码。
     *
     * @param category 错误分类（不能为空）
     * @return HttpStatus（非空）
     */
    HttpStatus map(ErrorCategory category);
}
