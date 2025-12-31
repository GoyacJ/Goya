package com.ysmjjsy.goya.component.web.scan;

import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 19:36
 */
public interface IRestMappingHandler {
    /**
     * RestMapping 处理
     * @param currentServiceId 当前服务Id ApplicationName
     * @param resources 资源
     */
    void handler(String currentServiceId, List<RestMapping> resources);

}
