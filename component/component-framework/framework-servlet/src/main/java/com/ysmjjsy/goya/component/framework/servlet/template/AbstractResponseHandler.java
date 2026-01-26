package com.ysmjjsy.goya.component.framework.servlet.template;

import com.ysmjjsy.goya.component.framework.core.api.ApiResponse;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Supplier;

/**
 * <p>抽象响应处理器</p>
 *
 * @author goya
 * @since 2025/12/29 18:59
 */
public abstract class AbstractResponseHandler {

    @Autowired
    private ThymeleafTemplateHandler templateHandler;

    protected void process(HttpServletRequest request, HttpServletResponse response, Supplier<ApiResponse<Void>> supplier, int statusCode) {

        ApiResponse<Void> voidResponse = supplier.get();

        if (WebUtils.isHtml(request)) {
            String content = null;
            if (supplier.get() != null) {
                content = templateHandler.renderToError(request, response, supplier.get());
            }
            if (StringUtils.isNotBlank(content)) {
                WebUtils.renderHtml(response, statusCode, content);
            } else {
                // 主要防止 Thymeleaf 模版转换有异常，做一项保护。
                WebUtils.renderResult(response, voidResponse, statusCode);
            }
        } else {
            WebUtils.renderResult(response, voidResponse, statusCode);
        }
    }
}
