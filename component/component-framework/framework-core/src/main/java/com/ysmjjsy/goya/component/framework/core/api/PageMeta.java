package com.ysmjjsy.goya.component.framework.core.api;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>分页元信息，用于在统一响应体中标准化表达分页相关数据</p>
 * <p>该类型位于 framework-core，不依赖 Web/Spring，适用于 Web、RPC、任务等多种场景。</p>
 *
 * <h2>约定</h2>
 * <ul>
 *   <li>pageNumber：页码（从 1 开始）</li>
 *   <li>pageSize：每页大小（> 0）</li>
 *   <li>totalElements：总记录数（>= 0）</li>
 *   <li>totalPages：总页数（根据 totalElements/pageSize 计算，至少为 0）</li>
 * </ul>
 *
 * @param pageNumber 页码（从 1 开始）
 * @param pageSize 每页大小
 * @param totalElements 总记录数
 * @param totalPages 总页数
 *
 * @author goya
 * @since 2026/1/24 14:53
 */
public record PageMeta(
        long pageNumber,
        long pageSize,
        long totalElements,
        long totalPages
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建分页元信息，并自动计算 totalPages。
     *
     * @param pageNumber 页码（从 1 开始）
     * @param pageSize 每页大小（>0）
     * @param totalElements 总记录数（>=0）
     * @return PageMeta
     */
    public static PageMeta of(long pageNumber, long pageSize, long totalElements) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber 必须从 1 开始");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize 必须大于 0");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements 不能小于 0");
        }
        long pages = (totalElements == 0) ? 0 : ((totalElements + pageSize - 1) / pageSize);
        return new PageMeta(pageNumber, pageSize, totalElements, pages);
    }

    /**
     * 是否存在下一页。
     *
     * @return 是否存在下一页
     */
    public boolean hasNext() {
        return totalPages > 0 && pageNumber < totalPages;
    }

    /**
     * 是否存在上一页。
     *
     * @return 是否存在上一页
     */
    public boolean hasPrev() {
        return totalPages > 0 && pageNumber > 1;
    }
}