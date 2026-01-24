package com.ysmjjsy.goya.component.framework.core.enums.dict;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.*;

/**
 * <p>CodeEnum 枚举 classpath 扫描器</p>
 *
 * <p>扫描范围：</p>
 * <ul>
 *   <li>使用 Spring Boot 的 {@link AutoConfigurationPackages} 获取应用基础包（通常是 @SpringBootApplication 所在包）。</li>
 *   <li>扫描这些包及子包下所有实现 {@link CodeEnum} 的类型，并筛选出 enum。</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/24 16:00
 */
public class CodeEnumClasspathScanner {


    private final ApplicationContext applicationContext;

    /**
     * 构造扫描器。
     *
     * @param applicationContext Spring 上下文
     */
    public CodeEnumClasspathScanner(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext 不能为空");
    }

    /**
     * 扫描并返回所有 CodeEnum 枚举类型。
     *
     * @return 枚举类型集合
     */
    public Set<Class<? extends Enum<?>>> scan() {
        List<String> basePackages = AutoConfigurationPackages.get(applicationContext);
        if (CollectionUtils.isEmpty(basePackages)) {
            return Set.of();
        }

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false, applicationContext.getEnvironment());
        scanner.addIncludeFilter(new AssignableTypeFilter(CodeEnum.class));

        Set<Class<? extends Enum<?>>> result = new LinkedHashSet<>();
        ClassLoader cl = applicationContext.getClassLoader();

        for (String pkg : basePackages) {
            for (var bd : scanner.findCandidateComponents(pkg)) {
                String className = bd.getBeanClassName();
                if (className == null) {
                    continue;
                }
                Class<?> type = loadClass(className, cl);
                if (type != null && type.isEnum() && CodeEnum.class.isAssignableFrom(type)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) type;
                    result.add(enumType);
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * 安全加载类。
     *
     * @param className 类名
     * @param cl 类加载器
     * @return Class 或 null
     */
    private Class<?> loadClass(String className, ClassLoader cl) {
        try {
            return Class.forName(className, false, cl);
        } catch (Throwable ignore) {
            return null;
        }
    }
}
