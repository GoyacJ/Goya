package com.ysmjjsy.goya.component.framework.common.utils;

import lombok.experimental.UtilityClass;
import org.lionsoul.ip2region.xdb.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>根据ip地址定位工具类，离线方式</p>
 *
 * @author goya
 * @since 2025/10/14 17:35
 */
@UtilityClass
public final class GoyaRegionUtils {

    private static final String IPV4_DB = "db/ip2region.xdb";
    private static final String IPV6_DB = "db/ipv6wry.db";

    private static final Searcher IPV4_SEARCHER;
    private static final Searcher IPV6_SEARCHER;

    private static final ReentrantReadWriteLock IPV4_LOCK = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock IPV6_LOCK = new ReentrantReadWriteLock();

    static {
        try {
            IPV4_SEARCHER = loadFromResource(IPV4_DB);
            IPV6_SEARCHER = loadFromResource(IPV6_DB);
        } catch (Exception e) {
            // 基础设施初始化失败，直接中止进程
            throw new ExceptionInInitializerError(e);
        }
    }

    /* =====================================================
     * Public API
     * ===================================================== */

    public static Region resolve(String ip) {
        if (ip == null || ip.isBlank()) {
            return Region.EMPTY;
        }

        // very fast IPv6 detection
        return ip.indexOf(':') >= 0
                ? resolveIpV6(ip)
                : resolveIpV4(ip);
    }

    public static Region resolveIpV4(String ip) {
        IPV4_LOCK.readLock().lock();
        try {
            String raw = IPV4_SEARCHER.search(ip);
            return parse(raw);
        } catch (Exception _) {
            return Region.EMPTY;
        } finally {
            IPV4_LOCK.readLock().unlock();
        }
    }

    public static Region resolveIpV6(String ip) {
        IPV6_LOCK.readLock().lock();
        try {
            String raw = IPV6_SEARCHER.search(ip);
            return parse(raw);
        } catch (Exception _) {
            return Region.EMPTY;
        } finally {
            IPV6_LOCK.readLock().unlock();
        }
    }

    /* =====================================================
     * Internal
     * ===================================================== */

    private static Searcher loadFromResource(String resourcePath) throws IOException, XdbException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = GoyaRegionUtils.class.getClassLoader();
        }

        try (InputStream is = cl.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException(
                        "ip2region resource not found: " + resourcePath);
            }

            // 1. load whole file into memory
            LongByteArray content = Searcher.loadContentFromInputStream(is);

            // 2. parse header
            Header header = Searcher.loadHeaderFromBuffer(content);

            // 3. detect IP version
            Version version = Version.fromHeader(header);

            // 4. verify structure
            Searcher.verify(header, content.length());

            // 5. create pure in-memory searcher (fastest)
            return Searcher.newWithBuffer(version, content);
        }
    }

    private static Region parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Region.EMPTY;
        }

        // ip2region format:
        // country|area|province|city|isp
        String[] parts = raw.split("\\|", -1);

        return new Region(
                part(parts, 0),
                part(parts, 1),
                part(parts, 2),
                part(parts, 3),
                part(parts, 4),
                raw
        );
    }

    private static String part(String[] parts, int index) {
        return index < parts.length ? parts[index] : "";
    }
}