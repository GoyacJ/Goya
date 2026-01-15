package com.ysmjjsy.goya.component.bus.stream.deserializer;

import com.ysmjjsy.goya.component.framework.context.SpringContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <p>事件类加载白名单</p>
 * <p>用于限制可以加载的事件类，防止恶意类加载攻击</p>
 * <p>默认只允许加载 com.ysmjjsy.goya 包下的类</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * EventClassWhitelist whitelist = new EventClassWhitelist();
 * whitelist.addAllowedPackage("com.ysmjjsy.goya");
 * whitelist.addAllowedPackage("com.example.events");
 *
 * if (whitelist.isAllowed("com.ysmjjsy.goya.events.OrderCreatedEvent")) {
 *     // 允许加载
 * } else {
 *     // 拒绝加载
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/27
 */
@Slf4j
public class EventClassWhitelist {

    /**
     * 允许的包名前缀列表
     */
    private final Set<String> allowedPackages = new CopyOnWriteArraySet<>();

    /**
     * 构造函数
     * <p>初始化时添加默认允许的包名</p>
     */
    public EventClassWhitelist() {
        this.allowedPackages.addAll(SpringContext.getPackageNames());
        log.debug("[Goya] |- component [bus] EventClassWhitelist |- initialized with default package: [{}]",
                allowedPackages);
    }

    /**
     * 构造函数
     * <p>使用指定的包名列表初始化</p>
     *
     * @param allowedPackages 允许的包名列表
     */
    public EventClassWhitelist(List<String> allowedPackages) {
        if (allowedPackages != null && !allowedPackages.isEmpty()) {
            this.allowedPackages.addAll(allowedPackages);
            this.allowedPackages.addAll(SpringContext.getPackageNames());
        } else {
            // 如果没有指定，使用默认包名
            this.allowedPackages.addAll(SpringContext.getPackageNames());
        }
        log.debug("[Goya] |- component [bus] EventClassWhitelist |- initialized with packages: [{}]",
                this.allowedPackages);
    }

    /**
     * 添加允许的包名
     *
     * @param packageName 包名（可以是完整包名或前缀）
     */
    public void addAllowedPackage(String packageName) {
        if (packageName != null && !packageName.isBlank()) {
            allowedPackages.add(packageName);
            log.debug("[Goya] |- component [bus] EventClassWhitelist |- added allowed package: [{}]",
                    packageName);
        }
    }

    /**
     * 移除允许的包名
     *
     * @param packageName 包名
     */
    public void removeAllowedPackage(String packageName) {
        if (packageName != null && !packageName.isBlank()) {
            allowedPackages.remove(packageName);
            log.debug("[Goya] |- component [bus] EventClassWhitelist |- removed allowed package: [{}]",
                    packageName);
        }
    }

    /**
     * 检查类名是否在白名单中
     * <p>如果类名以任何允许的包名开头，则允许加载</p>
     *
     * @param className 完整类名
     * @return true 如果允许加载
     */
    public boolean isAllowed(String className) {
        if (className == null || className.isBlank()) {
            log.warn("[Goya] |- component [bus] EventClassWhitelist |- className is null or blank, denied");
            return false;
        }

        for (String allowedPackage : allowedPackages) {
            if (className.startsWith(allowedPackage)) {
                log.trace("[Goya] |- component [bus] EventClassWhitelist |- className [{}] is allowed by package [{}]",
                        className, allowedPackage);
                return true;
            }
        }

        log.warn("[Goya] |- component [bus] EventClassWhitelist |- className [{}] is not in whitelist. " +
                        "Allowed packages: [{}]",
                className, allowedPackages);
        return false;
    }

    /**
     * 获取所有允许的包名
     *
     * @return 允许的包名集合
     */
    public Set<String> getAllowedPackages() {
        return Set.copyOf(allowedPackages);
    }

    /**
     * 清空白名单（不推荐使用，除非有特殊需求）
     */
    public void clear() {
        allowedPackages.clear();
        log.warn("[Goya] |- component [bus] EventClassWhitelist |- whitelist cleared. This may cause security issues.");
    }
}

