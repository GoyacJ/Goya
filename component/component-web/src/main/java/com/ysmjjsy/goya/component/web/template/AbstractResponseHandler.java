package com.ysmjjsy.goya.component.web.template;

import com.ysmjjsy.goya.component.web.response.Response;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;

import java.util.function.Supplier;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 18:59
 */
public abstract class AbstractResponseHandler {

    private final ThymeleafTemplateHandler templateHandler;

    protected AbstractResponseHandler(ThymeleafTemplateHandler templateHandler) {
        this.templateHandler = templateHandler;
    }

    protected void process(HttpServletRequest request, HttpServletResponse response, Supplier<ResponseEntity<Response<Void>>> supplier) {

        ResponseEntity<Response<Void>> result = supplier.get();

        if (WebUtils.isHtml(request)) {
            String content = null;
            if (result.getBody() != null) {
                content = templateHandler.renderToError(request, response, result.getBody());
            }
            if (StringUtils.isNotBlank(content)) {
                WebUtils.renderHtml(response, result.getBody().httpStatus().value(), content);
            } else {
                // 主要防止 Thymeleaf 模版转换有异常，做一项保护。
                WebUtils.renderResult(response, result.getBody());
            }
        } else {
            WebUtils.renderResult(response, result.getBody());
        }
    }
}
