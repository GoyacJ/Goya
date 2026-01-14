package com.ysmjjsy.goya.component.core.utils;

import lombok.experimental.UtilityClass;

import java.lang.management.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Java的JMX（Java Management Extensions）相关封装工具类。</p>
 * <p>JMX就是Java管理扩展，用来管理和监测 Java 程序。最常用到的就是对于 JVM 的监测和管理，比如 JVM 内存、CPU 使用率、线程数、垃圾收集情况等等。</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 20:03
 */
@UtilityClass
public class GoyaManagementUtils {

    // ==================== 内存信息 ====================

    /**
     * 获取堆内存使用情况
     *
     * @return 堆内存使用情况（字节）
     */
    public static long getHeapMemoryUsed() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getHeapMemoryUsage().getUsed();
    }

    /**
     * 获取堆内存提交大小
     *
     * @return 堆内存提交大小（字节）
     */
    public static long getHeapMemoryCommitted() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getHeapMemoryUsage().getCommitted();
    }

    /**
     * 获取堆内存最大大小
     *
     * @return 堆内存最大大小（字节），-1 表示无限制
     */
    public static long getHeapMemoryMax() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getHeapMemoryUsage().getMax();
    }

    /**
     * 获取堆内存初始大小
     *
     * @return 堆内存初始大小（字节）
     */
    public static long getHeapMemoryInit() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getHeapMemoryUsage().getInit();
    }

    /**
     * 获取堆内存使用率（百分比）
     *
     * @return 堆内存使用率（0.0 - 1.0）
     */
    public static double getHeapMemoryUsageRatio() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long max = heapUsage.getMax();
        if (max == -1) {
            // 无限制，使用提交大小作为分母
            max = heapUsage.getCommitted();
        }
        if (max == 0) {
            return 0.0;
        }
        return (double) heapUsage.getUsed() / max;
    }

    /**
     * 获取非堆内存使用情况
     *
     * @return 非堆内存使用情况（字节）
     */
    public static long getNonHeapMemoryUsed() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getNonHeapMemoryUsage().getUsed();
    }

    /**
     * 获取非堆内存提交大小
     *
     * @return 非堆内存提交大小（字节）
     */
    public static long getNonHeapMemoryCommitted() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getNonHeapMemoryUsage().getCommitted();
    }

    /**
     * 获取非堆内存最大大小
     *
     * @return 非堆内存最大大小（字节），-1 表示无限制
     */
    public static long getNonHeapMemoryMax() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getNonHeapMemoryUsage().getMax();
    }

    /**
     * 获取总内存使用情况（堆 + 非堆）
     *
     * @return 总内存使用情况（字节）
     */
    public static long getTotalMemoryUsed() {
        return getHeapMemoryUsed() + getNonHeapMemoryUsed();
    }

    /**
     * 获取所有内存池的使用情况
     *
     * @return 内存池信息列表
     */
    public static List<MemoryPoolMXBean> getMemoryPools() {
        return ManagementFactory.getMemoryPoolMXBeans();
    }

    /**
     * 执行垃圾回收
     */
    public static void gc() {
        ManagementFactory.getMemoryMXBean().gc();
    }

    // ==================== 线程信息 ====================

    /**
     * 获取当前线程数
     *
     * @return 当前线程数
     */
    public static int getThreadCount() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return threadBean.getThreadCount();
    }

    /**
     * 获取峰值线程数
     *
     * @return 峰值线程数
     */
    public static int getPeakThreadCount() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return threadBean.getPeakThreadCount();
    }

    /**
     * 获取总启动线程数
     *
     * @return 总启动线程数
     */
    public static long getTotalStartedThreadCount() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return threadBean.getTotalStartedThreadCount();
    }

    /**
     * 获取死锁线程ID数组
     *
     * @return 死锁线程ID数组
     */
    public static long[] findDeadlockedThreads() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        if (threadBean.isSynchronizerUsageSupported()) {
            return threadBean.findDeadlockedThreads();
        }
        return threadBean.findMonitorDeadlockedThreads();
    }

    /**
     * 检查是否存在死锁线程
     *
     * @return 是否存在死锁
     */
    public static boolean hasDeadlockedThreads() {
        long[] deadlockedThreads = findDeadlockedThreads();
        return deadlockedThreads != null && deadlockedThreads.length > 0;
    }

    /**
     * 获取所有线程信息
     *
     * @return 线程信息数组
     */
    public static ThreadInfo[] getAllThreadInfos() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return threadBean.dumpAllThreads(false, false);
    }

    /**
     * 获取指定线程的信息
     *
     * @param threadId 线程ID
     * @return 线程信息
     */
    public static ThreadInfo getThreadInfo(long threadId) {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return threadBean.getThreadInfo(threadId);
    }

    // ==================== CPU 信息 ====================

    /**
     * 获取进程CPU使用率（百分比）
     *
     * @return CPU使用率（0.0 - 1.0），如果不可用返回 -1
     */
    public static double getProcessCpuLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean os) {
            return os.getProcessCpuLoad();
        }
        return -1.0;
    }

    /**
     * 获取系统CPU使用率（百分比）
     *
     * @return CPU使用率（0.0 - 1.0），如果不可用返回 -1
     */
    public static double getSystemCpuLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean os) {
            return os.getCpuLoad();
        }
        return -1.0;
    }

    /**
     * 获取可用处理器数量
     *
     * @return 可用处理器数量
     */
    public static int getAvailableProcessors() {
        return ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
    }

    /**
     * 获取进程CPU时间（纳秒）
     *
     * @return CPU时间（纳秒），如果不可用返回 -1
     */
    public static long getProcessCpuTime() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuTime();
        }
        return -1L;
    }

    // ==================== 运行时信息 ====================

    /**
     * 获取JVM名称
     *
     * @return JVM名称
     */
    public static String getJvmName() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return runtimeBean.getVmName();
    }

    /**
     * 获取JVM版本
     *
     * @return JVM版本
     */
    public static String getJvmVersion() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return runtimeBean.getVmVersion();
    }

    /**
     * 获取JVM供应商
     *
     * @return JVM供应商
     */
    public static String getJvmVendor() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return runtimeBean.getVmVendor();
    }

    /**
     * 获取JVM启动时间（毫秒）
     *
     * @return JVM启动时间（毫秒）
     */
    public static long getJvmStartTime() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return runtimeBean.getStartTime();
    }

    /**
     * 获取JVM运行时间（毫秒）
     *
     * @return JVM运行时间（毫秒）
     */
    public static long getJvmUptime() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return runtimeBean.getUptime();
    }

    /**
     * 获取JVM输入参数
     *
     * @return JVM输入参数列表
     */
    public static List<String> getJvmInputArguments() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return runtimeBean.getInputArguments();
    }

    /**
     * 获取系统属性
     *
     * @return 系统属性
     */
    public static Map<String, String> getSystemProperties() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return runtimeBean.getSystemProperties();
    }

    /**
     * 获取类路径
     *
     * @return 类路径
     */
    public static String getClassPath() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return runtimeBean.getClassPath();
    }

    /**
     * 获取库路径
     *
     * @return 库路径
     */
    public static String getLibraryPath() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return runtimeBean.getLibraryPath();
    }

    /**
     * 获取引导类路径
     *
     * @return 引导类路径
     */
    public static String getBootClassPath() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return runtimeBean.getBootClassPath();
    }

    // ==================== 操作系统信息 ====================

    /**
     * 获取操作系统名称
     *
     * @return 操作系统名称
     */
    public static String getOsName() {
        return ManagementFactory.getOperatingSystemMXBean().getName();
    }

    /**
     * 获取操作系统版本
     *
     * @return 操作系统版本
     */
    public static String getOsVersion() {
        return ManagementFactory.getOperatingSystemMXBean().getVersion();
    }

    /**
     * 获取操作系统架构
     *
     * @return 操作系统架构
     */
    public static String getOsArch() {
        return ManagementFactory.getOperatingSystemMXBean().getArch();
    }

    /**
     * 判断是否为 Linux 系统
     *
     * @return 是否为 Linux 系统
     */
    public static boolean isLinux() {
        String osName = getOsName();
        return osName != null && osName.toLowerCase().contains("linux");
    }

    /**
     * 获取系统负载平均值（最近1分钟）
     *
     * @return 系统负载平均值，如果不可用返回 -1
     */
    public static double getSystemLoadAverage() {
        return ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
    }

    /**
     * 获取系统总物理内存（字节）
     *
     * @return 系统总物理内存（字节），如果不可用返回 -1
     */
    public static long getTotalPhysicalMemorySize() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean os) {
            return os.getTotalMemorySize();
        }
        return -1L;
    }

    /**
     * 获取系统可用物理内存（字节）
     *
     * @return 系统可用物理内存（字节），如果不可用返回 -1
     */
    public static long getFreePhysicalMemorySize() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getFreePhysicalMemorySize();
        }
        return -1L;
    }

    /**
     * 获取系统已用物理内存（字节）
     *
     * @return 系统已用物理内存（字节），如果不可用返回 -1
     */
    public static long getUsedPhysicalMemorySize() {
        long total = getTotalPhysicalMemorySize();
        long free = getFreePhysicalMemorySize();
        if (total == -1 || free == -1) {
            return -1L;
        }
        return total - free;
    }

    /**
     * 获取进程已用物理内存（字节）
     *
     * @return 进程已用物理内存（字节），如果不可用返回 -1
     */
    public static long getCommittedVirtualMemorySize() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean os) {
            return os.getCommittedVirtualMemorySize();
        }
        return -1L;
    }

    // ==================== 垃圾收集器信息 ====================

    /**
     * 获取所有垃圾收集器
     *
     * @return 垃圾收集器列表
     */
    public static List<GarbageCollectorMXBean> getGarbageCollectors() {
        return ManagementFactory.getGarbageCollectorMXBeans();
    }

    /**
     * 获取垃圾收集器名称列表
     *
     * @return 垃圾收集器名称列表
     */
    public static List<String> getGarbageCollectorNames() {
        List<GarbageCollectorMXBean> gcBeans = getGarbageCollectors();
        List<String> names = new ArrayList<>();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            names.add(gcBean.getName());
        }
        return names;
    }

    /**
     * 获取垃圾收集总次数
     *
     * @return 垃圾收集总次数
     */
    public static long getTotalGcCount() {
        long total = 0;
        for (GarbageCollectorMXBean gcBean : getGarbageCollectors()) {
            total += gcBean.getCollectionCount();
        }
        return total;
    }

    /**
     * 获取垃圾收集总时间（毫秒）
     *
     * @return 垃圾收集总时间（毫秒）
     */
    public static long getTotalGcTime() {
        long total = 0;
        for (GarbageCollectorMXBean gcBean : getGarbageCollectors()) {
            total += gcBean.getCollectionTime();
        }
        return total;
    }

    // ==================== 类加载信息 ====================

    /**
     * 获取已加载类数
     *
     * @return 已加载类数
     */
    public static long getLoadedClassCount() {
        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        return classBean.getLoadedClassCount();
    }

    /**
     * 获取总加载类数
     *
     * @return 总加载类数
     */
    public static long getTotalLoadedClassCount() {
        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        return classBean.getTotalLoadedClassCount();
    }

    /**
     * 获取已卸载类数
     *
     * @return 已卸载类数
     */
    public static long getUnloadedClassCount() {
        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        return classBean.getUnloadedClassCount();
    }

    /**
     * 检查是否支持类加载器详细监控
     *
     * @return 是否支持
     */
    public static boolean isVerboseClassLoading() {
        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        return classBean.isVerbose();
    }

    /**
     * 设置类加载器详细监控
     *
     * @param verbose 是否启用
     */
    public static void setVerboseClassLoading(boolean verbose) {
        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        classBean.setVerbose(verbose);
    }

    // ==================== 编译信息 ====================

    /**
     * 获取编译器信息
     *
     * @return 编译器信息，如果不可用返回 null
     */
    public static CompilationMXBean getCompiler() {
        return ManagementFactory.getCompilationMXBean();
    }

    /**
     * 获取编译器名称
     *
     * @return 编译器名称，如果不可用返回 null
     */
    public static String getCompilerName() {
        CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();
        return compilationBean != null ? compilationBean.getName() : null;
    }

    /**
     * 检查是否支持编译时间监控
     *
     * @return 是否支持
     */
    public static boolean isCompilationTimeMonitoringSupported() {
        CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();
        return compilationBean != null && compilationBean.isCompilationTimeMonitoringSupported();
    }

    /**
     * 获取总编译时间（毫秒）
     *
     * @return 总编译时间（毫秒），如果不可用返回 -1
     */
    public static long getTotalCompilationTime() {
        CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();
        if (compilationBean != null && compilationBean.isCompilationTimeMonitoringSupported()) {
            return compilationBean.getTotalCompilationTime();
        }
        return -1L;
    }

    // ==================== 工具方法 ====================

    /**
     * 格式化字节数为可读格式
     *
     * @param bytes 字节数
     * @return 格式化后的字符串（如：1.5 MB）
     */
    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "N/A";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }
        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * 格式化毫秒数为可读格式
     *
     * @param millis 毫秒数
     * @return 格式化后的字符串（如：1h 30m 15s）
     */
    public static String formatUptime(long millis) {
        if (millis < 0) {
            return "N/A";
        }
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");
        return sb.toString();
    }

    /**
     * 格式化百分比
     *
     * @param ratio 比率（0.0 - 1.0）
     * @return 格式化后的百分比字符串（如：75.5%）
     */
    public static String formatPercent(double ratio) {
        if (ratio < 0) {
            return "N/A";
        }
        return String.format("%.2f%%", ratio * 100);
    }
}
