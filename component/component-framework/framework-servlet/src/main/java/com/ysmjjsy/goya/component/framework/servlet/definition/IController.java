package com.ysmjjsy.goya.component.framework.servlet.definition;

import com.ysmjjsy.goya.component.framework.core.api.ApiResponse;
import com.ysmjjsy.goya.component.framework.core.pojo.PageVO;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <p>base controller interface</p>
 *
 * @author goya
 * @since 2025/12/19 22:33
 */
public interface IController {

    /**
     * 数据实体转换为统一响应实体
     *
     * @return {@link com.ysmjjsy.goya.component.framework.core.api.ApiResponse} Entity
     */
    default ApiResponse<Void> response() {
        return ApiResponse.ok();
    }

    /**
     * 数据实体转换为统一响应实体
     *
     * @param domain 数据实体
     * @param <D>    {@link Serializable} 子类型
     * @return {@link ApiResponse} Entity
     */
    default <D extends Serializable> ApiResponse<D> response(D domain) {
        return ApiResponse.ok(domain);
    }

    /**
     * 数据列表转换为统一响应实体
     *
     * @param domains 数据实体 List
     * @param <D>     {@link Serializable} 子类型
     * @return {@link ApiResponse} List
     */
    default <D extends Serializable> ApiResponse<List<D>> response(List<D> domains) {
        return ApiResponse.ok(domains);
    }

    /**
     * 数组转换为统一响应实体
     *
     * @param domains 数组
     * @param <T>     数组类型
     * @return {@link ApiResponse} List
     */
    default <T> ApiResponse<T[]> response(T[] domains) {
        return ApiResponse.ok(domains);
    }

    /**
     * 数据 Map 转换为统一响应实体
     *
     * @param map 数据 Map
     * @return {@link ApiResponse} Map
     */
    default ApiResponse<Map<String, Object>> response(Map<String, Object> map) {
        return ApiResponse.ok(map);
    }

    /**
     * 数据操作结果转换为统一响应实体
     *
     * @param status 操作状态
     * @return {@link ApiResponse} String
     */
    default ApiResponse<Boolean> response(Boolean status) {
        return ApiResponse.ok(status);
    }

    /**
     * 数据实体转换为统一响应实体
     *
     * @param page 数据实体
     * @param <E>  {@link Serializable} 子类型
     * @return {@link ApiResponse} Entity
     */
    default <E extends Serializable> ApiResponse<PageVO<E>> response(PageVO<E> page) {
        return ApiResponse.ok(page);
    }
}
