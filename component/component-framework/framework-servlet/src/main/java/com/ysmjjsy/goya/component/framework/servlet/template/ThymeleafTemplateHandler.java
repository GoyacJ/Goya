package com.ysmjjsy.goya.component.framework.servlet.template;

import com.ysmjjsy.goya.component.web.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/8 22:17
 */
public class ThymeleafTemplateHandler {

    private final SpringTemplateEngine springTemplateEngine;

    public ThymeleafTemplateHandler(SpringTemplateEngine springTemplateEngine) {
        this.springTemplateEngine = springTemplateEngine;
    }

    public String renderToError(HttpServletRequest request, HttpServletResponse response, Response<Void> result) {
        return render(request, response, "error", result.toErrorModel());
    }

    public String render(String template, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return springTemplateEngine.process(template, context);
    }

    public String render(HttpServletRequest request, HttpServletResponse response, String template, Map<String, Object> model) {
        final IWebExchange webExchange = JakartaServletWebApplication
                .buildApplication(request.getServletContext())
                .buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariables(model);
        return springTemplateEngine.process(template, context);
    }
}
