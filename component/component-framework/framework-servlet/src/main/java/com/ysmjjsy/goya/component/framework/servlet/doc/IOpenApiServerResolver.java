package com.ysmjjsy.goya.component.framework.servlet.doc;

import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/8 20:44
 */
public interface IOpenApiServerResolver {

    /**
     * 获取 Open Api 所需的 Server 地址。
     *
     * @return Open Api Servers 值
     */
    List<Server> getServers();
}
