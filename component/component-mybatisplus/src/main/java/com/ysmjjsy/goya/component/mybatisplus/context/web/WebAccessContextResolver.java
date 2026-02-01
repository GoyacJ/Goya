package com.ysmjjsy.goya.component.mybatisplus.context.web;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.security.domain.SubjectType;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextResolver;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextValue;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>基于请求头的访问上下文解析器。</p>
 *
 * <p>默认读取 X-User-Id，并映射为 Subject(USER)。</p>
 *
 * @author goya
 * @since 2026/1/31 13:30
 */
public class WebAccessContextResolver implements AccessContextResolver {

    @Override
    public AccessContextValue resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String userId = headerOrNull(request, DefaultConst.X_USER_ID);
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("userId", userId);
        String tenantId = headerOrNull(request, DefaultConst.X_TENANT_ID);
        if (StringUtils.hasText(tenantId)) {
            attrs.put("tenantId", tenantId);
        }
        return new AccessContextValue(userId, SubjectType.USER, userId, attrs);
    }

    private String headerOrNull(HttpServletRequest request, String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        String value = request.getHeader(name);
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
