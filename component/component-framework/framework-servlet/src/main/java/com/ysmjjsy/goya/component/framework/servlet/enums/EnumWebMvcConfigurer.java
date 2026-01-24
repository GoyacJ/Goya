package com.ysmjjsy.goya.component.framework.servlet.enums;

import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>枚举体系 Web MVC 配置：注册请求参数绑定转换器。</p>
 *
 * @author goya
 * @since 2026/1/24 15:54
 */
public class EnumWebMvcConfigurer implements WebMvcConfigurer {

    /**
     * 向 Spring MVC 注册 ConverterFactory。
     *
     * @param registry 注册表
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringToCodeEnumConverterFactory());
    }
}
