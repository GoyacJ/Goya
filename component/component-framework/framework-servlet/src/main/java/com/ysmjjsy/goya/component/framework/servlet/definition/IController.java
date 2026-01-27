package com.ysmjjsy.goya.component.framework.servlet.definition;

import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
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
     * @return {@link ApiRes} Entity
     */
    default ApiRes<Void> response() {
        return ApiRes.ok();
    }

    /**
     * 数据实体转换为统一响应实体
     *
     * @param domain 数据实体
     * @param <D>    {@link Serializable} 子类型
     * @return {@link ApiRes} Entity
     */
    default <D extends Serializable> ApiRes<D> response(D domain) {
        return ApiRes.ok(domain);
    }

    /**
     * 数据列表转换为统一响应实体
     *
     * @param domains 数据实体 List
     * @param <D>     {@link Serializable} 子类型
     * @return {@link ApiRes} List
     */
    default <D extends Serializable> ApiRes<List<D>> response(List<D> domains) {
        return ApiRes.ok(domains);
    }

    /**
     * 数组转换为统一响应实体
     *
     * @param domains 数组
     * @param <T>     数组类型
     * @return {@link ApiRes} List
     */
    default <T> ApiRes<T[]> response(T[] domains) {
        return ApiRes.ok(domains);
    }

    /**
     * 数据 Map 转换为统一响应实体
     *
     * @param map 数据 Map
     * @return {@link ApiRes} Map
     */
    default ApiRes<Map<String, Object>> response(Map<String, Object> map) {
        return ApiRes.ok(map);
    }

    /**
     * 数据操作结果转换为统一响应实体
     *
     * @param status 操作状态
     * @return {@link ApiRes} String
     */
    default ApiRes<Boolean> response(Boolean status) {
        return ApiRes.ok(status);
    }

    /**
     * 数据实体转换为统一响应实体
     *
     * @param page 数据实体
     * @param <E>  {@link Serializable} 子类型
     * @return {@link ApiRes} Entity
     */
    default <E extends Serializable> ApiRes<PageVO<E>> response(PageVO<E> page) {
        return ApiRes.ok(page);
    }
}
