package com.ysmjjsy.goya.component.framework.servlet.crypto;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * <p>加密解密标记注解</p>
 *
 * @author goya
 * @since 2025/10/9 09:42
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface Crypto {

    /**
     * 请求参数记否解密，默认值 true
     *
     * @return true 请求参数解密；false 请求参数不解密
     */
    boolean requestDecrypt() default true;

    /**
     * 响应体是否加密，默认值 true
     *
     * @return true 响应体加密；false 响应体不加密
     */
    boolean responseEncrypt() default true;
}
