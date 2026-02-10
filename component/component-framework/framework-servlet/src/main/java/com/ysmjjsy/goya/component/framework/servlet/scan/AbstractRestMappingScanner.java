package com.ysmjjsy.goya.component.framework.servlet.scan;

import com.ysmjjsy.goya.component.framework.core.processor.ApplicationInitializingEvent;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.GoyaWebProperties;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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

    private final GoyaWebProperties.Scan scan;
    private final IRestMappingHandler iRestMappingHandler;

    protected AbstractRestMappingScanner(GoyaWebProperties properties, IRestMappingHandler iRestMappingHandler) {
        this.scan = properties.scan();
        this.iRestMappingHandler = iRestMappingHandler;
    }


    @Override
    public void onApplicationEvent(ApplicationInitializingEvent event) {
        if (!Boolean.TRUE.equals(scan.enabled())) {
            log.debug("[Goya] |- Request Mapping Scan disabled by property [goya.web.scan.enabled=false]");
            return;
        }
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
        if (StringUtils.isBlank(className)) {
            return true;
        }

        List<String> groupIds = scan.scanGroupIds();
        if (CollectionUtils.isEmpty(groupIds)) {
            return true;
        }

        return groupIds.stream()
                .filter(StringUtils::isNotBlank)
                .anyMatch(className::contains);
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
        String resultCode = RestMappingCodeUtils.createMappingCode(requestMethods, url);
        log.trace("[Goya] |- Create code [{}] for Request [{}] : [{}]", resultCode, requestMethods, url);
        return resultCode;
    }

    /**
     * 扫描完成操作
     *
     * @param serviceId 服务ID
     * @param resources 扫描到的资源
     */
    protected void complete(String serviceId, List<RestMapping> resources) {
        if (CollectionUtils.isNotEmpty(resources)) {
            log.debug("[Goya] |- [R2] Request mapping scan found [{}] resources in service [{}], go to next stage!", serviceId, resources.size());
            if (ObjectUtils.isNotEmpty(iRestMappingHandler)) {
                iRestMappingHandler.handler(serviceId, resources);
            }
        } else {
            log.debug("[Goya] |- [R2] Request mapping scan can not find any resources in service [{}]!", serviceId);
        }

        log.debug("[Goya] |- Request Mapping Scan for Service: [{}] FINISHED!", serviceId);
    }
}
