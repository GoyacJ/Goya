package com.ysmjjsy.goya.component.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/9/15 10:45
 */
@Slf4j
public class WebPlatformService extends AbstractPlatformService {

    public WebPlatformService(ServerProperties serverProperties) {
        super(serverProperties);
    }
}
