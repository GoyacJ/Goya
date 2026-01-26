package com.ysmjjsy.goya.component.framework.servlet.context;

import com.ysmjjsy.goya.component.framework.common.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.core.context.GoyaContext;
import com.ysmjjsy.goya.component.framework.core.context.GoyaUser;
import com.ysmjjsy.goya.component.framework.core.context.TenantContext;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;

import java.util.Objects;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/13 14:19
 */
@Slf4j
public abstract class AbstractGoyaContext implements GoyaContext {

    private final ServerProperties serverProperties;

    protected AbstractGoyaContext(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
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
            return WebUtils.addressToUri(address, getProperties().protocol(), true);
        }
        return null;
    }

    @Override
    public String getContextPath() {
        String contextPath = serverProperties.getServlet().getContextPath();
        if (StringUtils.isNotBlank(contextPath) && !Strings.CS.equals(contextPath, SymbolConst.FORWARD_SLASH)) {
            return WebUtils.robustness(contextPath, SymbolConst.FORWARD_SLASH, false, false);
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public String getAuthServiceUri() {
        return getUrl();
    }

    @Override
    public String getAuthServiceName() {
        return WebUtils.getApplicationName();
    }

    @Override
    public GoyaUser currentUser() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GoyaUser currentUser(HttpServletRequest request) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GoyaUser currentUser(String token) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String currentTenant() {
        return TenantContext.getTenantId();
    }
}
