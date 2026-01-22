package com.ysmjjsy.goya.component.web.template;

import com.ysmjjsy.goya.component.web.response.Response;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Supplier;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 18:59
 */
public abstract class AbstractResponseHandler {

    @Autowired
    private ThymeleafTemplateHandler templateHandler;

    protected void process(HttpServletRequest request, HttpServletResponse response, Supplier<Response<Void>> supplier) {

        Response<Void> voidResponse = supplier.get();

        if (WebUtils.isHtml(request)) {
            String content = null;
            if (supplier.get() != null) {
                content = templateHandler.renderToError(request, response, supplier.get());
            }
            if (StringUtils.isNotBlank(content)) {
                WebUtils.renderHtml(response, voidResponse.httpStatus().value(), content);
            } else {
                // 主要防止 Thymeleaf 模版转换有异常，做一项保护。
                WebUtils.renderResult(response, voidResponse);
            }
        } else {
            WebUtils.renderResult(response, voidResponse);
        }
    }
}
