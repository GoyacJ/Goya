package com.ysmjjsy.goya.component.framework.servlet.scan;

import com.ysmjjsy.goya.component.framework.common.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaMD5Utils;
import com.ysmjjsy.goya.component.framework.core.context.GoyaContext;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.GoyaWebProperties;
import com.ysmjjsy.goya.component.framework.servlet.enums.RequestMethodEnum;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/9 16:24
 */
@Slf4j
public class WebRestMappingScanner extends AbstractRestMappingScanner {

    private final GoyaContext goyaContext;

    public WebRestMappingScanner(GoyaWebProperties properties,
                                 ObjectProvider<IRestMappingHandler> iRestMappingHandlerObjectProvider,
                                 GoyaContext goyaContext) {
        super(properties, iRestMappingHandlerObjectProvider.getIfAvailable());
        this.goyaContext = goyaContext;
    }


    @Override
    protected void onScanner(ApplicationContext applicationContext) {

        // 1. 获取服务ID:该服务ID对于微服务是必需的。
        String serviceId = WebUtils.getApplicationName(applicationContext);
        log.debug("[Goya] |- [R1] Application is READY, service[{}] start to scan request mapping!", serviceId);

        // 2. 封装并转换为RestMapping
        List<RestMapping> resources = applicationContext.getBeansOfType(RequestMappingHandlerMapping.class)
                .values()
                .stream()
                .map(RequestMappingHandlerMapping::getHandlerMethods)
                .filter(MapUtils::isNotEmpty)
                .flatMap(hm -> hm.entrySet().stream())
                .filter(e -> !isExcludedRequestMapping(e.getValue()))
                .flatMap(e -> createRestMappings(serviceId, e.getKey(), e.getValue()).stream())
                .toList();
        complete(serviceId, resources);
    }

    private List<RestMapping> createRestMappings(String serviceId, RequestMappingInfo info, HandlerMethod method) {
        // 校验是否忽略
        Boolean elementIgnore = checkIgnore(method);
        if (elementIgnore == null) {
            return List.of();
        }

        // 获取类名
        // method.getMethod().getDeclaringClass().getName() 取到的是注解实际所在类的名字，比如注解在父类叫BaseController，那么拿到的就是BaseController
        // method.getBeanType().getName() 取到的是注解实际Bean的名字，比如注解在在父类叫BaseController，而实际类是SysUserController，那么拿到的就是SysUserController
        String className = method.getBeanType().getName();

        // 检测该类是否在 GroupIds 允许列表中
        if (!isLegalGroup(className)) {
            return List.of();
        }

        // 获取RequestMapping注解对应的方法名
        String methodName = method.getMethod().getName();

        Set<RequestMethodEnum> requestMethodEnums = RequestMethodEnum.parseRequestMethods(info.getMethodsCondition().getMethods());

        Set<String> patternValues = getPatternValues(info);
        if (CollectionUtils.isEmpty(patternValues)) {
            return List.of();
        }

        Operation apiOperation = method.getMethodAnnotation(Operation.class);
        Tag tag = method.getBeanType().getAnnotation(Tag.class);

        List<RestMapping> mappings = new ArrayList<>();
        for (String patternValue : patternValues) {
            if (org.apache.commons.lang3.StringUtils.isBlank(patternValue)) {
                continue;
            }
            String mappingUrl = getUrl(patternValue);
            for (RequestMethodEnum requestMethodEnum : requestMethodEnums) {
                String methodCode = requestMethodEnum == null ? RequestMethodEnum.ALL.getCode() : requestMethodEnum.getCode();
                String flag = serviceId + SymbolConst.DASH + methodCode + SymbolConst.DASH + patternValue;
                String id = GoyaMD5Utils.md5(flag);

                RestMapping restMapping = new RestMapping();
                restMapping.setMappingId(id);
                restMapping.setMappingCode(createCode(patternValue, methodCode));
                restMapping.setServiceId(serviceId);
                restMapping.setRequestMethod(Set.of(requestMethodEnum == null ? RequestMethodEnum.ALL : requestMethodEnum));
                restMapping.setUrl(mappingUrl);
                restMapping.setBaseUrl(getBaseUrl(method));
                restMapping.setClassName(className);
                restMapping.setMethodName(methodName);
                restMapping.setElementIgnore(elementIgnore);
                if (Objects.nonNull(apiOperation)) {
                    restMapping.setSummary(apiOperation.summary());
                    restMapping.setDescription(apiOperation.description());
                }
                if (Objects.nonNull(tag)) {
                    restMapping.setTag(tag.name());
                }
                mappings.add(restMapping);
            }
        }

        return mappings;
    }

    /**
     * 校验是否忽略
     *
     * @param method 方法
     * @return 是否忽略: null忽略
     */
    private @org.jspecify.annotations.Nullable Boolean checkIgnore(HandlerMethod method) {
        Scan scan = method.getBeanType().getAnnotation(Scan.class);
        if (Objects.nonNull(scan) && scan.ignore()) {
            return null;
        }

        Scan methodScan = method.getMethodAnnotation(Scan.class);
        if (Objects.nonNull(methodScan) && methodScan.ignore()) {
            return null;
        }

        boolean elementIgnore = false;
        if (Objects.nonNull(scan)) {
            elementIgnore = scan.elementIgnore();
        }

        if (Objects.nonNull(methodScan)) {
            elementIgnore = methodScan.elementIgnore();
        }
        return elementIgnore;
    }

    /**
     * 获取url
     *
     * @param patternValues urls
     * @return url
     */
    private String getUrls(Set<String> patternValues) {
        if (CollectionUtils.isEmpty(patternValues)) {
            return "";
        }

        return patternValues.stream().map(this::getUrl).collect(Collectors.joining(SymbolConst.COMMA));
    }

    private String getUrl(String patternValue) {
        if (org.apache.commons.lang3.StringUtils.isBlank(patternValue)) {
            return "";
        }
        if (goyaContext.hasContextPath()) {
            return goyaContext.getContextPath() + patternValue;
        }
        return patternValue;
    }

    /**
     * 获取urls
     *
     * @param info
     * @return
     */
    private Set<String> getPatternValues(RequestMappingInfo info) {
        PathPatternsRequestCondition condition = info.getPathPatternsCondition();
        return (condition != null ? condition.getPatternValues() : null);
    }

    /**
     * 获取base url
     *
     * @param method
     * @return
     */
    private String getBaseUrl(HandlerMethod method) {
        Class<?> beanClass = method.getBeanType();
        RequestMapping classMapping = beanClass.getAnnotation(RequestMapping.class);

        Set<String> classUrls = new HashSet<>();
        if (classMapping != null) {
            String[] value = classMapping.value();
            classUrls.addAll(Arrays.asList(value));
        }
        return getUrls(classUrls);
    }
}
