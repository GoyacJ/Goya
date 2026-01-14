package com.ysmjjsy.goya.component.framework.context;

import com.ysmjjsy.goya.component.core.utils.GoyaStringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.thread.Threading;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:44
 */
@Slf4j
public class SpringContext implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Getter
    private static ConfigurableApplicationContext context;

    public static boolean isVirtual() {
        return Threading.VIRTUAL.isActive(SpringContext.getBean(Environment.class));
    }

    public static BeanFactory getBeanFactory() {
        return context;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        SpringContext.context = applicationContext;
    }

    /* -------------------- Bean 获取 -------------------- */

    /**
     * 根据Bean名称获取Bean实例
     *
     * @param name Bean名称
     * @param <T>  Bean类型
     * @return Bean实例
     * @throws IllegalStateException                    如果ApplicationContext未注入
     * @throws org.springframework.beans.BeansException 如果Bean不存在
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) context.getBean(name);
    }

    /**
     * 根据Bean类型获取Bean实例
     *
     * @param requiredType Bean类型
     * @param <T>          Bean类型
     * @return Bean实例
     * @throws IllegalStateException                    如果ApplicationContext未注入
     * @throws org.springframework.beans.BeansException 如果Bean不存在或存在多个
     */
    public static <T> T getBean(Class<T> requiredType) {
        return context.getBean(requiredType);
    }

    /**
     * 根据Bean名称和类型获取Bean实例
     *
     * @param name         Bean名称
     * @param requiredType Bean类型
     * @param <T>          Bean类型
     * @return Bean实例
     * @throws IllegalStateException                    如果ApplicationContext未注入
     * @throws org.springframework.beans.BeansException 如果Bean不存在或类型不匹配
     */
    public static <T> T getBean(String name, Class<T> requiredType) {
        return context.getBean(name, requiredType);
    }

    /**
     * 安全获取Bean，不存在返回null
     *
     * @param name Bean名称
     * @param <T>  Bean类型
     * @return Bean实例，不存在返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBeanOrNull(String name) {
        try {
            return (T) context.getBean(name);
        } catch (Exception e) {
            log.debug("[SpringContext] getBeanOrNull Bean不存在: {}", name, e);
            return null;
        }
    }

    /**
     * 安全获取Bean，不存在返回null
     *
     * @param requiredType Bean类型
     * @param <T>          Bean类型
     * @return Bean实例，不存在返回null
     */
    public static <T> T getBeanOrNull(Class<T> requiredType) {
        try {
            return context.getBean(requiredType);
        } catch (Exception e) {
            log.debug("[SpringContext] getBeanOrNull Bean不存在: {}", requiredType.getName(), e);
            return null;
        }
    }

    /**
     * 安全获取Bean，返回Optional
     *
     * @param name Bean名称
     * @param <T>  Bean类型
     * @return Optional包装的Bean实例
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getBeanOptional(String name) {
        try {
            return Optional.of((T) context.getBean(name));
        } catch (Exception e) {
            log.debug("[SpringContext] Bean不存在: {}", name, e);
            return Optional.empty();
        }
    }

    /**
     * 安全获取Bean，返回Optional
     *
     * @param requiredType Bean类型
     * @param <T>          Bean类型
     * @return Optional包装的Bean实例
     */
    public static <T> Optional<T> getBeanOptional(Class<T> requiredType) {
        try {
            return Optional.of(context.getBean(requiredType));
        } catch (Exception e) {
            log.debug("[SpringContext] Bean不存在: {}", requiredType.getName(), e);
            return Optional.empty();
        }
    }

    /**
     * 延迟获取Bean（通过Supplier）
     *
     * @param supplier Bean获取的Supplier
     * @param <T>      Bean类型
     * @return Bean实例
     */
    public static <T> T getBeanLazy(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * 判断容器中是否包含指定名称的Bean
     *
     * @param name Bean名称
     * @return true表示存在，false表示不存在
     */
    public static boolean containsBean(String name) {
        return context.containsBean(name);
    }

    /**
     * 判断指定名称的Bean是否为单例
     *
     * @param name Bean名称
     * @return true表示是单例，false表示不是单例
     */
    public static boolean isSingleton(String name) {
        return context.isSingleton(name);
    }

    /**
     * 获取指定名称Bean的类型
     *
     * @param name Bean名称
     * @return Bean类型，不存在返回null
     */
    public static Class<?> getType(String name) {
        return context.getType(name);
    }

    /**
     * 获取指定类型的所有Bean名称
     *
     * @param type Bean类型
     * @param <T>  Bean类型
     * @return Bean名称数组
     */
    public static <T> String[] getBeanNamesOfType(Class<T> type) {
        return context.getBeanNamesForType(type);
    }

    /**
     * 获取带指定注解的所有Bean名称
     *
     * @param annotationClass 注解类型
     * @param <A>             注解类型
     * @return Bean名称数组
     */
    public static <A extends Annotation> String[] getBeanNamesWithAnnotation(Class<A> annotationClass) {
        return context.getBeanNamesForAnnotation(annotationClass);
    }

    /* -------------------- 注解 Bean -------------------- */

    /**
     * 获取所有带指定注解的Bean
     *
     * @param annotationClass 注解类型
     * @param <A>             注解类型
     * @return Bean实例列表
     */
    public static <A extends Annotation> List<Object> getBeansWithAnnotation(Class<A> annotationClass) {
        Map<String, Object> beans = context.getBeansWithAnnotation(annotationClass);
        return new ArrayList<>(beans.values());
    }

    /**
     * 获取指定类型且带指定注解的Bean
     *
     * @param requiredType    Bean类型
     * @param annotationClass 注解类型
     * @param <T>             Bean类型
     * @param <A>             注解类型
     * @return Bean实例列表
     */
    public static <T, A extends Annotation> List<T> getBeansOfTypeWithAnnotation(Class<T> requiredType, Class<A> annotationClass) {
        Map<String, T> beans = context.getBeansOfType(requiredType);
        return beans.values().stream()
                .filter(bean -> AnnotationUtils.findAnnotation(bean.getClass(), annotationClass) != null)
                .toList();
    }

    /**
     * 获取指定类型的所有Bean
     *
     * @param type Bean类型
     * @param <T>  Bean类型
     * @return Bean实例列表
     */
    public static <T> List<T> getBeansOfType(Class<T> type) {
        Map<String, T> beans = context.getBeansOfType(type);
        return new ArrayList<>(beans.values());
    }

    /**
     * 获取指定类型的所有Bean
     *
     * @param type Bean类型
     * @param <T>  Bean类型
     * @return Bean实例列表
     */
    public static <T> Map<String, T> getBeanMapsOfType(Class<T> type) {
        return context.getBeansOfType(type);
    }

    /**
     * 获取指定类型的所有Bean
     *
     * @param type Bean类型
     * @return Bean实例列表
     */
    public static <A extends Annotation> Map<String, Object> getBeanMapsOfAnnotation(Class<A> type) {
        return context.getBeansWithAnnotation(type);
    }

    /* -------------------- 包路径 Bean -------------------- */

    /**
     * 获取指定包路径下的所有Bean
     *
     * @param basePackage 基础包路径
     * @return Bean实例列表
     */
    public static List<Object> getBeansInPackage(String basePackage) {
        Map<String, Object> allBeans = context.getBeansOfType(Object.class);
        return allBeans.values().stream()
                .filter(bean -> ClassUtils.getPackageName(bean.getClass()).startsWith(basePackage))
                .toList();
    }

    /**
     * 获取指定包路径下指定类型的所有Bean
     *
     * @param basePackage 基础包路径
     * @param type        Bean类型
     * @param <T>         Bean类型
     * @return Bean实例列表
     */
    public static <T> List<T> getBeansInPackage(String basePackage, Class<T> type) {
        Map<String, T> allBeans = context.getBeansOfType(type);
        return allBeans.values().stream()
                .filter(bean -> ClassUtils.getPackageName(bean.getClass()).startsWith(basePackage))
                .toList();
    }

    /* -------------------- Bean 批量操作 -------------------- */

    /**
     * 批量按类型操作Bean
     *
     * @param type     Bean类型
     * @param consumer 操作函数
     * @param <T>      Bean类型
     */
    public static <T> void forEachBeanOfType(Class<T> type, Consumer<T> consumer) {
        getBeansOfType(type).forEach(consumer);
    }

    /**
     * 批量按注解操作Bean
     *
     * @param annotationClass 注解类型
     * @param consumer        操作函数
     * @param <A>             注解类型
     */
    public static <A extends Annotation> void forEachBeanWithAnnotation(Class<A> annotationClass, Consumer<Object> consumer) {
        getBeansWithAnnotation(annotationClass).forEach(consumer);
    }

    /**
     * 按条件筛选Bean
     *
     * @param predicate 筛选条件
     * @return 符合条件的Bean实例列表
     */
    public static List<Object> getBeansByPredicate(Predicate<Object> predicate) {
        Map<String, Object> allBeans = context.getBeansOfType(Object.class);
        return allBeans.values().stream()
                .filter(predicate)
                .toList();
    }

    /* -------------------- Bean 注解信息 -------------------- */

    /**
     * 获取指定Bean的所有注解
     *
     * @param beanName Bean名称
     * @return 注解列表
     */
    public static List<Annotation> getBeanAnnotations(String beanName) {
        Object bean = getBean(beanName);
        return Arrays.asList(bean.getClass().getAnnotations());
    }

    /**
     * 获取所有Bean定义名称
     *
     * @return Bean名称数组
     */
    public static String[] getBeanDefinitionNames() {
        return context.getBeanDefinitionNames();
    }

    /**
     * 获取Bean总数
     *
     * @return Bean总数
     */
    public static int getBeanCount() {
        return context.getBeanDefinitionCount();
    }

    /**
     * 判断指定名称的Bean是否为原型（非单例）
     *
     * @param name Bean名称
     * @return true表示是原型，false表示不是原型
     */
    public static boolean isPrototype(String name) {
        return context.isPrototype(name);
    }

    /**
     * 获取Bean的别名
     *
     * @param name Bean名称
     * @return 别名数组
     */
    public static String[] getBeanAliases(String name) {
        return context.getAliases(name);
    }

    /* -------------------- Spring Profile -------------------- */

    /**
     * 判断当前环境是否包含指定Profile
     *
     * @param profile Profile名称
     * @return true表示包含，false表示不包含
     */
    public static boolean hasProfile(String profile) {
        Environment env = context.getEnvironment();
        return Arrays.asList(env.getActiveProfiles()).contains(profile);
    }

    /**
     * 获取指定Profile下的Bean
     *
     * @param profile Profile名称
     * @param type    Bean类型
     * @param <T>     Bean类型
     * @return Bean实例列表
     */
    public static <T> List<T> getBeansForProfile(String profile, Class<T> type) {
        if (!hasProfile(profile)) {
            return Collections.emptyList();
        }
        return getBeansOfType(type);
    }

    /**
     * 获取激活的Profile列表
     *
     * @return Profile名称数组
     */
    public static String[] getActiveProfiles() {
        return context.getEnvironment().getActiveProfiles();
    }

    /**
     * 获取默认Profile列表
     *
     * @return Profile名称数组
     */
    public static String[] getDefaultProfiles() {
        return context.getEnvironment().getDefaultProfiles();
    }

    /**
     * 判断是否为开发环境
     *
     * @return true表示是开发环境
     */
    public static boolean isDev() {
        return hasProfile("dev") || hasProfile("development");
    }

    /**
     * 判断是否为生产环境
     *
     * @return true表示是生产环境
     */
    public static boolean isProd() {
        return hasProfile("prod") || hasProfile("production");
    }

    /**
     * 判断是否为测试环境
     *
     * @return true表示是测试环境
     */
    public static boolean isTest() {
        return hasProfile("test");
    }

    /* -------------------- Environment 配置属性 -------------------- */

    /**
     * 获取配置属性
     *
     * @param key 属性键
     * @return 属性值，不存在返回null
     */
    public static String getProperty(String key) {
        return getProperty(context.getEnvironment(), key);
    }

    /**
     * 获取配置属性（带默认值）
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值，不存在返回默认值
     */
    public static String getProperty(String key, String defaultValue) {
        return getProperty(context.getEnvironment(), key, defaultValue);
    }

    /**
     * 获取配置属性（指定类型）
     *
     * @param key        属性键
     * @param targetType 目标类型
     * @param <T>        目标类型
     * @return 属性值，不存在返回null
     */
    public static <T> T getProperty(String key, Class<T> targetType) {
        return getProperty(context.getEnvironment(), key, targetType);
    }

    /**
     * 从环境信息中获取配置信息
     *
     * @param environment Spring Boot Environment {@link Environment}
     * @param property    配置名称
     * @return 配置属性值
     */
    public static String getProperty(Environment environment, String property) {
        return environment.getProperty(property);
    }

    /**
     * 从环境信息中获取配置信息
     *
     * @param environment  Spring Boot Environment {@link Environment}
     * @param property     配置名称
     * @param defaultValue 默认值
     * @return 配置属性值
     */
    public static String getProperty(Environment environment, String property, String defaultValue) {
        return environment.getProperty(property, defaultValue);
    }

    /**
     * 从条件上下文中获取配置信息
     *
     * @param conditionContext Spring Boot ConditionContext {@link ConditionContext}
     * @param property         配置名称
     * @return 配置属性值
     */
    public static String getProperty(ConditionContext conditionContext, String property) {
        return getProperty(conditionContext.getEnvironment(), property);
    }

    /**
     * 从条件上下文中获取配置信息
     *
     * @param conditionContext Spring Boot ConditionContext {@link ConditionContext}
     * @param property         配置名称
     * @param defaultValue     默认值
     * @return 配置属性值
     */
    public static String getProperty(ConditionContext conditionContext, String property, String defaultValue) {
        return getProperty(conditionContext.getEnvironment(), property, defaultValue);
    }

    /**
     * 从条件上下文中获取配置信息
     *
     * @param environment Spring Boot Environment {@link Environment}
     * @param property    配置名称
     * @param targetType  目标类型
     * @param <T>         目标类型
     * @return 配置属性值
     */
    public static <T> T getProperty(Environment environment, String property, Class<T> targetType) {
        return environment.getProperty(property, targetType);
    }

    /**
     * 从条件上下文中获取配置信息
     *
     * @param environment  Spring Boot Environment {@link Environment}
     * @param property     配置名称
     * @param targetType   目标类型
     * @param defaultValue 默认值
     * @param <T>          目标类型
     * @return 配置属性值
     */
    public static <T> T getProperty(Environment environment, String property, Class<T> targetType, T defaultValue) {
        return environment.getProperty(property, targetType, defaultValue);
    }

    /**
     * 从条件上下文中获取配置信息
     *
     * @param conditionContext Spring Boot ConditionContext {@link ConditionContext}
     * @param property         配置名称
     * @param targetType       目标类型
     * @param <T>              目标类型
     * @return 配置属性值
     */
    public static <T> T getProperty(ConditionContext conditionContext, String property, Class<T> targetType) {
        return getProperty(conditionContext.getEnvironment(), property, targetType);
    }

    /**
     * 从条件上下文中获取配置信息
     *
     * @param conditionContext Spring Boot ConditionContext {@link ConditionContext}
     * @param property         配置名称
     * @param targetType       目标类型
     * @param defaultValue     默认值
     * @param <T>              目标类型
     * @return 配置属性值
     */
    public static <T> T getProperty(ConditionContext conditionContext, String property, Class<T> targetType, T defaultValue) {
        return getProperty(conditionContext.getEnvironment(), property, targetType, defaultValue);
    }

    /**
     * 条件上下文中是否包含配置信息
     *
     * @param environment Spring Boot Environment {@link Environment}
     * @param property    配置名称
     * @return 配置属性值
     */
    public static boolean contains(Environment environment, String property) {
        return environment.containsProperty(property);
    }

    /**
     * 条件上下文中是否包含配置信息
     *
     * @param property 配置名称
     * @return 配置属性值
     */
    public static boolean contains(String property) {
        return contains(context.getEnvironment(), property);
    }

    /**
     * 条件上下文中是否包含配置信息
     *
     * @param conditionContext Spring Boot ConditionContext {@link ConditionContext}
     * @param property         配置名称
     * @return 配置属性值
     */
    public static boolean contains(ConditionContext conditionContext, String property) {
        return contains(conditionContext.getEnvironment(), property);
    }

    /**
     * 获取必需的配置属性
     *
     * @param key 属性键
     * @return 属性值
     * @throws IllegalStateException 如果属性不存在
     */
    public static String getRequiredProperty(String key) {
        return getRequiredProperty(context.getEnvironment(), key);
    }

    /**
     * 获取必需的配置属性
     *
     * @param key 属性键
     * @return 属性值
     * @throws IllegalStateException 如果属性不存在
     */
    public static String getRequiredProperty(Environment environment, String key) {
        return environment.getRequiredProperty(key);
    }

    /**
     * 从条件上下文中获取Boolean类型值配置信息
     *
     * @param environment  Spring Boot ConditionContext {@link ConditionContext}
     * @param property     配置名称
     * @param defaultValue 默认值
     * @return 配置属性值
     */
    public static boolean getBoolean(Environment environment, String property, boolean defaultValue) {
        return getProperty(environment, property, Boolean.class, defaultValue);
    }

    /**
     * 从条件上下文中获取Boolean类型值配置信息
     *
     * @param environment Spring Boot ConditionContext {@link ConditionContext}
     * @param property    配置名称
     * @return 配置属性值
     */
    public static boolean getBoolean(Environment environment, String property) {
        return getProperty(environment, property, Boolean.class, false);
    }

    /**
     * 从条件上下文中获取Boolean类型值配置信息
     *
     * @param conditionContext Spring Boot ConditionContext {@link ConditionContext}
     * @param property         配置名称
     * @return 配置属性值
     */
    public static boolean getBoolean(ConditionContext conditionContext, String property) {
        return getProperty(conditionContext, property, Boolean.class, false);
    }

    /**
     * 从条件上下文中获取Boolean类型值配置信息
     *
     * @param conditionContext Spring Boot ConditionContext {@link ConditionContext}
     * @param property         配置名称
     * @param defaultValue     默认值
     * @return 配置属性值
     */
    public static boolean getBoolean(ConditionContext conditionContext, String property, boolean defaultValue) {
        return getProperty(conditionContext, property, Boolean.class, defaultValue);
    }

    /* -------------------- 事件发布 -------------------- */

    /**
     * 发布 Spring 事件
     *
     * @param event 事件对象
     */
    public static void publishEvent(Object event) {
        context.publishEvent(event);
    }

    /* -------------------- 资源访问 -------------------- */

    /**
     * 获取资源
     *
     * @param location 资源位置
     * @return 资源对象
     */
    public static Resource getResource(String location) {
        return context.getResource(location);
    }

    /**
     * 获取多个资源
     *
     * @param locationPattern 资源位置模式
     * @return 资源数组
     */
    public static Resource[] getResources(String locationPattern) {
        try {
            return context.getResources(locationPattern);
        } catch (Exception e) {
            log.error("[SpringContext] 获取资源失败: {}", locationPattern, e);
            return new Resource[0];
        }
    }

    /**
     * 获取资源流
     *
     * @param location 资源位置
     * @return 资源输入流
     */
    public static InputStream getResourceAsStream(String location) {
        Resource resource = getResource(location);
        try {
            return resource.getInputStream();
        } catch (Exception e) {
            log.error("[SpringContext] 获取资源流失败: {}", location, e);
            return null;
        }
    }

    /**
     * 根据资源位置解析并返回 {@link File} 对象。
     * <p>
     * 该方法仅在底层资源实际存在于文件系统中时才有效。
     * 如果资源位于 classpath（如 jar 包内）或是远程 URL，
     * 调用该方法将抛出 {@link IOException}。
     *
     * @param location 资源位置，支持 classpath:、file:、URL 等形式
     * @return 对应的 {@link File} 对象
     * @throws IOException 如果资源无法解析为文件，或资源不存在
     */
    public static File getFile(String location) throws IOException {
        return getResource(location).getFile();
    }

    /**
     * 根据资源位置获取对应的 {@link InputStream}。
     * <p>
     * 适用于任意类型的 Spring {@link org.springframework.core.io.Resource}，
     * 包括文件资源、classpath 资源、jar 内资源以及远程资源。
     * <p>
     * 调用方需负责关闭返回的输入流。
     *
     * @param location 资源位置，支持 classpath:、classpath*:、file:、URL 等
     * @return 资源的输入流
     * @throws IOException 如果资源不存在或无法打开输入流
     */
    public static InputStream getInputStream(String location) throws IOException {
        return getResource(location).getInputStream();
    }

    /**
     * 获取资源的文件名部分。
     * <p>
     * 返回值通常为路径的最后一段，不包含目录信息。
     * 对于某些资源类型（如匿名流资源），可能返回 {@code null}。
     *
     * @param location 资源位置
     * @return 资源文件名，如果无法确定则返回 {@code null}
     */
    public static String getFilename(String location) {
        return getResource(location).getFilename();
    }

    /**
     * 获取资源的 {@link URI} 表示形式。
     * <p>
     * 该 URI 可用于进一步的路径解析或与其他 API 交互。
     * 对于 jar 内资源，返回的 URI 通常为 {@code jar:file:...} 形式。
     *
     * @param location 资源位置
     * @return 资源对应的 {@link URI}
     * @throws IOException 如果 URI 无法解析
     */
    public static URI getUri(String location) throws IOException {
        return getResource(location).getURI();
    }

    /**
     * 获取资源的 {@link URL} 表示形式。
     * <p>
     * 对于 classpath 资源、文件资源或远程资源，
     * 返回的 URL 类型可能不同（file、jar、http 等）。
     *
     * @param location 资源位置
     * @return 资源对应的 {@link URL}
     * @throws IOException 如果 URL 无法解析
     */
    public static URL getUrl(String location) throws IOException {
        return getResource(location).getURL();
    }

    /**
     * 获取资源内容的长度（字节数）。
     * <p>
     * 对于部分资源类型（如流式资源），
     * 底层实现可能需要实际读取内容以计算长度。
     *
     * @param location 资源位置
     * @return 资源内容长度（字节）
     * @throws IOException 如果资源不存在或长度无法确定
     */
    public static long contentLength(String location) throws IOException {
        return getResource(location).contentLength();
    }

    /**
     * 获取资源的最后修改时间。
     * <p>
     * 返回值通常为自 Unix Epoch（1970-01-01T00:00:00Z）以来的毫秒数。
     * 对于不支持修改时间的资源类型，可能抛出异常。
     *
     * @param location 资源位置
     * @return 最后修改时间的时间戳（毫秒）
     * @throws IOException 如果无法获取修改时间
     */
    public static long lastModified(String location) throws IOException {
        return getResource(location).lastModified();
    }

    /**
     * 判断指定位置的资源是否存在。
     * <p>
     * 该方法不会打开资源内容，仅用于存在性检查。
     *
     * @param location 资源位置
     * @return 如果资源存在则返回 {@code true}，否则返回 {@code false}
     */
    public static boolean exists(String location) {
        return getResource(location).exists();
    }

    /**
     * 判断资源是否表示为文件系统中的文件。
     * <p>
     * 返回 {@code true} 表示资源可以通过 {@link File} 访问。
     * jar 内资源、远程资源通常返回 {@code false}。
     *
     * @param location 资源位置
     * @return 如果资源是文件系统文件则返回 {@code true}
     */
    public static boolean isFile(String location) {
        return getResource(location).isFile();
    }

    /**
     * 判断资源是否可读。
     * <p>
     * 可读并不保证资源一定存在，但通常意味着可以成功打开输入流。
     *
     * @param location 资源位置
     * @return 如果资源可读则返回 {@code true}
     */
    public static boolean isReadable(String location) {
        return getResource(location).isReadable();
    }

    /**
     * 判断资源是否处于已打开状态。
     * <p>
     * 大多数资源类型始终返回 {@code false}。
     * 对于某些流式资源，若返回 {@code true}，表示资源只能被读取一次。
     *
     * @param location 资源位置
     * @return 如果资源已打开则返回 {@code true}
     */
    public static boolean isOpen(String location) {
        return getResource(location).isOpen();
    }

    /**
     * 判断给定位置字符串是否为 URL 形式。
     * <p>
     * 支持的 URL 前缀包括 {@code file:}、{@code http:}、{@code https:}、
     * {@code jar:}、{@code classpath:} 等。
     *
     * @param location 资源位置字符串
     * @return 如果是 URL 形式则返回 {@code true}
     */
    public static boolean isUrl(String location) {
        return ResourceUtils.isUrl(location);
    }

    /**
     * 判断资源位置是否为 {@code classpath:} 前缀的类路径资源。
     *
     * @param location 资源位置
     * @return 如果是 {@code classpath:} 资源则返回 {@code true}
     */
    public static boolean isClasspathUrl(String location) {
        return Strings.CS.startsWith(location, ResourceLoader.CLASSPATH_URL_PREFIX);
    }

    /**
     * 判断资源位置是否为 {@code classpath*:} 前缀的类路径通配资源。
     * <p>
     * {@code classpath*:} 通常用于加载多个 classpath 下的同名资源。
     *
     * @param location 资源位置
     * @return 如果是 {@code classpath*:} 资源则返回 {@code true}
     */
    public static boolean isClasspathAllUrl(String location) {
        return GoyaStringUtils.startsWithAny(
                location, ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX);
    }

    /**
     * 判断给定 {@link URL} 是否为 jar 协议的 URL。
     *
     * @param url 要检查的 URL
     * @return 如果是 jar URL 则返回 {@code true}
     */
    public static boolean isJarUrl(URL url) {
        return ResourceUtils.isJarURL(url);
    }

    /**
     * 判断给定 {@link URL} 是否为文件系统（file 协议）的 URL。
     *
     * @param url 要检查的 URL
     * @return 如果是 file URL 则返回 {@code true}
     */
    public static boolean isFileUrl(URL url) {
        return ResourceUtils.isFileURL(url);
    }

    /**
     * 将 {@link Resource} 转换为 byte
     *
     * @param resource 资源 {@link Resource}
     * @return byte 数组
     */
    public static byte[] toBytes(Resource resource) {
        try {
            InputStream inputStream = resource.getInputStream();
            return FileCopyUtils.copyToByteArray(inputStream);
        } catch (IOException e) {
            log.error("[GOYA] |- Converter resource to byte[] error!", e);
            return null;
        }
    }

    /**
     * 将 {@link Resource} 转换为 Base64 数据。
     * <p>
     * 例如：将图片类型的 Resource 转换为可以直接在前端展现的 Base64 数据
     *
     * @param resource 资源 {@link Resource}
     * @return Base64 类型的字符串
     */
    public static String toBase64(Resource resource) {
        byte[] bytes = toBytes(resource);
        return Base64.getEncoder().encodeToString(bytes);
    }
    /* -------------------- 国际化 -------------------- */

    /**
     * 获取国际化消息
     *
     * @param code 消息代码
     * @param args 参数
     * @return 国际化消息
     */
    public static String getMessage(String code, Object... args) {
        try {
            return context.getMessage(code, args, null);
        } catch (Exception e) {
            log.debug("[SpringContext] 获取国际化消息失败: {}", code, e);
            return null;
        }
    }

    /**
     * 获取国际化消息（带默认值）
     *
     * @param code           消息代码
     * @param args           参数
     * @param defaultMessage 默认消息
     * @return 国际化消息
     */
    public static String getMessage(String code, Object[] args, String defaultMessage) {
        return context.getMessage(code, args, defaultMessage, null);
    }

    /**
     * 获取国际化消息（指定Locale）
     *
     * @param code           消息代码
     * @param args           参数
     * @param defaultMessage 默认消息
     * @param locale         Locale
     * @return 国际化消息
     */
    public static String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return context.getMessage(code, args, defaultMessage, locale);
    }

    /* -------------------- 类型转换 -------------------- */

    /**
     * 类型转换
     *
     * @param source     源对象
     * @param targetType 目标类型
     * @param <T>        目标类型
     * @return 转换后的对象，转换失败返回null
     */
    public static <T> T convert(Object source, Class<T> targetType) {
        try {
            org.springframework.core.convert.ConversionService conversionService =
                    getBeanOrNull(org.springframework.core.convert.ConversionService.class);
            if (conversionService == null) {
                log.debug("[SpringContext] ConversionService不存在，无法进行类型转换");
                return null;
            }
            return conversionService.convert(source, targetType);
        } catch (Exception e) {
            log.debug("[SpringContext] 类型转换失败: {} -> {}", source.getClass().getName(), targetType.getName(), e);
            return null;
        }
    }

    /**
     * 必要时转换类型（如果已经是目标类型则直接返回）
     *
     * @param value        值
     * @param requiredType 目标类型
     * @param <T>          目标类型
     * @return 转换后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertIfNecessary(Object value, Class<T> requiredType) {
        if (value == null) {
            return null;
        }
        if (requiredType.isInstance(value)) {
            return (T) value;
        }
        return convert(value, requiredType);
    }

    /* -------------------- 工具方法 -------------------- */

    /**
     * 获取类加载器
     *
     * @return 类加载器
     */
    public static ClassLoader getClassLoader() {
        return context.getClassLoader();
    }

    /**
     * 获取应用启动时间
     *
     * @return 启动时间戳（毫秒）
     */
    public static long getStartupDate() {
        return context.getStartupDate();
    }

    /**
     * 获取应用显示名称
     *
     * @return 显示名称
     */
    public static String getDisplayName() {
        return context.getDisplayName();
    }

    /**
     * 获取应用ID
     *
     * @return 应用ID
     */
    public static String getId() {
        return context.getId();
    }
}
