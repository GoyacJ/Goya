package com.ysmjjsy.goya.component.framework.servlet.context;

import org.springframework.boot.web.server.autoconfigure.ServerProperties;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/16 15:18
 */
public class GoyaWebContext extends AbstractGoyaContext {

    public GoyaWebContext(ServerProperties serverProperties) {
        super(serverProperties);
    }
}
