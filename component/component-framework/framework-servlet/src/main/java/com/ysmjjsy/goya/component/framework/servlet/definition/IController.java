package com.ysmjjsy.goya.component.framework.servlet.definition;

import com.ysmjjsy.goya.component.framework.pojo.PageVO;
import com.ysmjjsy.goya.component.web.response.Response;

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
     * @return {@link Response} Entity
     */
    default Response<Void> response() {
        return Response.success();
    }

    /**
     * 数据实体转换为统一响应实体
     *
     * @param domain 数据实体
     * @param <D>    {@link Serializable} 子类型
     * @return {@link Response} Entity
     */
    default <D extends Serializable> Response<D> response(D domain) {
        return Response.success(domain);
    }

    /**
     * 数据列表转换为统一响应实体
     *
     * @param domains 数据实体 List
     * @param <D>     {@link Serializable} 子类型
     * @return {@link Response} List
     */
    default <D extends Serializable> Response<List<D>> response(List<D> domains) {
        return Response.success(domains);
    }

    /**
     * 数组转换为统一响应实体
     *
     * @param domains 数组
     * @param <T>     数组类型
     * @return {@link Response} List
     */
    default <T> Response<T[]> response(T[] domains) {
        return Response.success(domains);
    }

    /**
     * 数据 Map 转换为统一响应实体
     *
     * @param map 数据 Map
     * @return {@link Response} Map
     */
    default Response<Map<String, Object>> response(Map<String, Object> map) {
        return Response.success(map);
    }

    /**
     * 数据操作结果转换为统一响应实体
     *
     * @param status 操作状态
     * @return {@link Response} String
     */
    default Response<Boolean> response(Boolean status) {
        return Response.success(status);
    }

    /**
     * 数据实体转换为统一响应实体
     *
     * @param page 数据实体
     * @param <E>  {@link Serializable} 子类型
     * @return {@link Response} Entity
     */
    default <E extends Serializable> Response<PageVO<E>> response(PageVO<E> page) {
        return Response.success(page);
    }
}
