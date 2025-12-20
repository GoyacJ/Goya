package com.ysmjjsy.goya.component.common.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.thread.Threading;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:44
 */
@Slf4j
public class SpringContext implements ApplicationContextInitializer<ConfigurableApplicationContext> {

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
    /* -------------------- ApplicationContext -------------------- */

    /**
     * 获取ApplicationContext实例
     *
     * @return ApplicationContext实例
     * @throws IllegalStateException 如果ApplicationContext未注入
     */
    public static ApplicationContext getApplicationContext() {
        return context;
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
            log.debug("[SpringUtils] Bean不存在: {}", name);
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
            log.debug("[SpringUtils] Bean不存在: {}", requiredType.getName());
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
            return Optional.ofNullable((T) context.getBean(name));
        } catch (Exception e) {
            log.debug("[SpringUtils] Bean不存在: {}", name);
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
            return Optional.ofNullable(context.getBean(requiredType));
        } catch (Exception e) {
            log.debug("[SpringUtils] Bean不存在: {}", requiredType.getName());
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
        return context.getEnvironment().getProperty(key);
    }

    /**
     * 获取配置属性（带默认值）
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值，不存在返回默认值
     */
    public static String getProperty(String key, String defaultValue) {
        return context.getEnvironment().getProperty(key, defaultValue);
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
        return context.getEnvironment().getProperty(key, targetType);
    }

    /**
     * 获取必需的配置属性
     *
     * @param key 属性键
     * @return 属性值
     * @throws IllegalStateException 如果属性不存在
     */
    public static String getRequiredProperty(String key) {
        return context.getEnvironment().getRequiredProperty(key);
    }

    /* -------------------- 事件发布 -------------------- */

    /**
     * 发布Spring事件
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
    public static org.springframework.core.io.Resource getResource(String location) {
        return context.getResource(location);
    }

    /**
     * 获取多个资源
     *
     * @param locationPattern 资源位置模式
     * @return 资源数组
     */
    public static org.springframework.core.io.Resource[] getResources(String locationPattern) {
        try {
            return context.getResources(locationPattern);
        } catch (Exception e) {
            log.error("[SpringUtils] 获取资源失败: {}", locationPattern, e);
            return new org.springframework.core.io.Resource[0];
        }
    }

    /**
     * 获取资源流
     *
     * @param location 资源位置
     * @return 资源输入流
     */
    public static java.io.InputStream getResourceAsStream(String location) {
        org.springframework.core.io.Resource resource = getResource(location);
        if (resource == null) {
            return null;
        }
        try {
            return resource.getInputStream();
        } catch (Exception e) {
            log.error("[SpringUtils] 获取资源流失败: {}", location, e);
            return null;
        }
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
            log.debug("[SpringUtils] 获取国际化消息失败: {}", code);
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
    public static String getMessage(String code, Object[] args, String defaultMessage, java.util.Locale locale) {
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
                log.debug("[SpringUtils] ConversionService不存在，无法进行类型转换");
                return null;
            }
            return conversionService.convert(source, targetType);
        } catch (Exception e) {
            log.debug("[SpringUtils] 类型转换失败: {} -> {}", source.getClass().getName(), targetType.getName());
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
