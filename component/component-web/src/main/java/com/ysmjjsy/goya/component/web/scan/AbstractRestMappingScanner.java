package com.ysmjjsy.goya.component.web.scan;

import com.ysmjjsy.goya.component.common.context.ApplicationInitializingEvent;
import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import com.ysmjjsy.goya.component.common.utils.CommonUtils;
import com.ysmjjsy.goya.component.web.configuration.properties.WebProperties;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 19:31
 */
@Slf4j
public abstract class AbstractRestMappingScanner implements ApplicationListener<ApplicationInitializingEvent> {

    private final WebProperties.Scan scan;
    private final IRestMappingHandler iRestMappingHandler;

    protected AbstractRestMappingScanner(WebProperties properties, IRestMappingHandler iRestMappingHandler) {
        this.scan = properties.scan();
        this.iRestMappingHandler = iRestMappingHandler;
    }


    @Override
    public void onApplicationEvent(ApplicationInitializingEvent event) {
        onScanner(event.getContext());
    }

    protected abstract void onScanner(ApplicationContext applicationContext);


    /**
     * 检测RequestMapping是否需要被排除
     *
     * @param handlerMethod HandlerMethod
     * @return boolean
     */
    protected boolean isExcludedRequestMapping(HandlerMethod handlerMethod) {
        if (!isSpringAnnotationMatched(handlerMethod)) {
            return true;
        }

        return !isSwaggerAnnotationMatched(handlerMethod);
    }

    /**
     * 如果开启isJustScanRestController，那么就只扫描RestController
     *
     * @param handlerMethod HandlerMethod
     * @return boolean
     */
    protected boolean isSpringAnnotationMatched(HandlerMethod handlerMethod) {
        if (Boolean.TRUE.equals(scan.justScanRestController())) {
            return handlerMethod.getMethod().getDeclaringClass().getAnnotation(RestController.class) != null;
        }

        return true;
    }

    /**
     * 有ApiIgnore注解的方法不扫描, 没有ApiOperation注解不扫描
     *
     * @param handlerMethod HandlerMethod
     * @return boolean
     */
    protected boolean isSwaggerAnnotationMatched(HandlerMethod handlerMethod) {
        if (handlerMethod.getMethodAnnotation(Hidden.class) != null) {
            return false;
        }

        Operation operation = handlerMethod.getMethodAnnotation(Operation.class);
        return ObjectUtils.isNotEmpty(operation) && !operation.hidden();
    }

    /**
     * 如果当前class的groupId在GroupId列表中，那么就进行扫描，否则就排除
     *
     * @param className 当前扫描的controller类名
     * @return Boolean
     */
    protected boolean isLegalGroup(String className) {
        if (StringUtils.isNotEmpty(className)) {
            List<String> groupIds = scan.scanGroupIds();
            List<String> result = groupIds.stream().filter(groupId -> Strings.CS.contains(className, groupId)).toList();
            return CollectionUtils.isEmpty(result);
        } else {
            return true;
        }
    }

    /**
     * 根据 url 和 method 生成与当前 url 对应的 code。
     * <p>
     * 例如：
     * 1. POST /element 生成为 post:element
     * 2. /open/identity/session 生成为 open:identity:session
     *
     * @param url            请求 url
     * @param requestMethods 请求 method。
     * @return url 对应的 code
     */
    protected String createCode(String url, String requestMethods) {
        String[] search = new String[]{ISymbolConstants.OPEN_CURLY_BRACE, ISymbolConstants.CLOSE_CURLY_BRACE, ISymbolConstants.FORWARD_SLASH};
        String[] replacement = new String[]{ISymbolConstants.BLANK, ISymbolConstants.BLANK, ISymbolConstants.COLON};
        String code = StringUtils.replaceEach(url, search, replacement);

        String resultCode = StringUtils.isNotBlank(requestMethods) ? StringUtils.lowerCase(requestMethods) + code : Strings.CS.removeStart(code, ISymbolConstants.COLON);
        log.trace("[GOYA] |- Create code [{}] for Request [{}] : [{}]", resultCode, requestMethods, url);
        return resultCode;
    }

    /**
     * 将接口 URL 转换为权限 code
     * 例如: /water/swMaintenanceTasks/add -> water:sw_maintenance_tasks:add
     */
    public static String urlToPermissionCode(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // 去除首尾的斜杠
        url = url.trim();
        if (url.startsWith(ISymbolConstants.FORWARD_SLASH)) {
            url = url.substring(1);
        }
        if (url.endsWith(ISymbolConstants.FORWARD_SLASH)) {
            url = url.substring(0, url.length() - 1);
        }

        String[] parts = url.split(ISymbolConstants.FORWARD_SLASH);
        if (parts.length < 3) {
            // 不足三级路径，直接返回下划线连接
            return String.join(":", parts);
        }

        // 模块前缀部分 (例如: water)
        String module = parts[0];

        // 资源部分 (例如: swMaintenanceTasks -> sw_maintenance_tasks)
        String resource = CommonUtils.humpToLine(parts[1]);

        // 操作部分 (例如: add / deleteBatch / exportXls)
        String action = parts[2];

        return String.format("%s:%s:%s", module, resource, action);
    }

    /**
     * 扫描完成操作
     *
     * @param serviceId 服务ID
     * @param resources 扫描到的资源
     */
    protected void complete(String serviceId, List<RestMapping> resources) {
        if (CollectionUtils.isNotEmpty(resources)) {
            log.debug("[GOYA] |- [R2] Request mapping scan found [{}] resources in service [{}], go to next stage!", serviceId, resources.size());
            if (ObjectUtils.isNotEmpty(iRestMappingHandler)) {
                iRestMappingHandler.handler(serviceId, resources);
            }
        } else {
            log.debug("[GOYA] |- [R2] Request mapping scan can not find any resources in service [{}]!", serviceId);
        }

        log.debug("[GOYA] |- Request Mapping Scan for Service: [{}] FINISHED!", serviceId);
    }
}
