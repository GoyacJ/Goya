package com.ysmjjsy.goya.component.framework.servlet.configuration;

import com.google.common.collect.Lists;
import com.ysmjjsy.goya.component.framework.servlet.crypto.DecryptRequestParamMapResolver;
import com.ysmjjsy.goya.component.framework.servlet.crypto.DecryptRequestParamResolver;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/31 11:08
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class DecryptConfiguration {

    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;
    private final DecryptRequestParamResolver decryptRequestParamResolver;
    private final DecryptRequestParamMapResolver decryptRequestParamMapResolver;

    @PostConstruct
    public void init() {
        List<HandlerMethodArgumentResolver> unmodifiableList = requestMappingHandlerAdapter.getArgumentResolvers();
        List<HandlerMethodArgumentResolver> list = Lists.newArrayList();
        for (HandlerMethodArgumentResolver methodArgumentResolver : unmodifiableList) {
            //需要在requestParam之前执行自定义逻辑，然后再执行下一个逻辑（责任链模式）
            if (methodArgumentResolver instanceof RequestParamMapMethodArgumentResolver) {
                decryptRequestParamMapResolver.setRequestParamMapMethodArgumentResolver((RequestParamMapMethodArgumentResolver) methodArgumentResolver);
                list.add(decryptRequestParamMapResolver);
            }
            if (methodArgumentResolver instanceof RequestParamMethodArgumentResolver) {
                decryptRequestParamResolver.setRequestParamMethodArgumentResolver((RequestParamMethodArgumentResolver) methodArgumentResolver);
                list.add(decryptRequestParamResolver);
            }
            list.add(methodArgumentResolver);
        }
        log.debug("[Goya] |- component [web] DecryptAutoConfiguration auto configure.");

        requestMappingHandlerAdapter.setArgumentResolvers(list);
    }
}
