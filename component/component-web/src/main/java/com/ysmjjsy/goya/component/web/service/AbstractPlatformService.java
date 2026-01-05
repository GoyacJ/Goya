package com.ysmjjsy.goya.component.web.service;

import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import com.ysmjjsy.goya.component.common.service.IPlatformService;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Objects;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/13 14:19
 */
@Slf4j
public abstract class AbstractPlatformService implements IPlatformService, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final ServerProperties serverProperties;

    protected AbstractPlatformService(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Integer getPort() {
        return Objects.nonNull(serverProperties.getPort()) ? serverProperties.getPort() : 8080;
    }

    @Override
    public String getIp() {
        String hostAddress = WebUtils.getHostAddress();
        if (Objects.nonNull(serverProperties.getAddress())) {
            hostAddress = serverProperties.getAddress().getHostAddress();
        }

        if (StringUtils.isNotBlank(hostAddress)) {
            return hostAddress;
        }
        return "localhost";
    }

    @Override
    public String getUrl() {
        String address = getAddress();
        if (StringUtils.isNotBlank(address)) {
            return WebUtils.addressToUri(address, getPlatformProperties().protocol(), true);
        }
        return null;
    }

    @Override
    public String getContextPath() {
        String contextPath = serverProperties.getServlet().getContextPath();
        if (StringUtils.isNotBlank(contextPath) && !Strings.CS.equals(contextPath, ISymbolConstants.FORWARD_SLASH)) {
            return WebUtils.robustness(contextPath, ISymbolConstants.FORWARD_SLASH, false, false);
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public String getAuthServiceUri() {
        return "";
    }

    @Override
    public String getAuthServiceName() {
        return "";
    }
}
