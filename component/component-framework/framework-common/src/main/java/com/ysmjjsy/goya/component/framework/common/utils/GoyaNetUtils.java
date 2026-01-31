package com.ysmjjsy.goya.component.framework.common.utils;

import com.ysmjjsy.goya.component.framework.common.enums.IpTypeEnum;
import com.ysmjjsy.goya.component.framework.common.enums.RegexPoolEnum;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.net.*;
import java.util.Enumeration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/14 17:39
 */
@UtilityClass
public class GoyaNetUtils {
    /**
     * 判断是否为 IPv4 地址
     *
     * @param ip IP 地址
     * @return 是否为 IPv4
     */
    public static boolean isIpv4(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        return RegexPoolEnum.IPV4_REGEX.matches(ip);
    }

    /**
     * 判断是否为 IPv6 地址
     *
     * @param ip IP 地址
     * @return 是否为 IPv6
     */
    public static boolean isIpv6(String ip) {
        try {
            return InetAddress.getByName(ip) instanceof Inet6Address;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 判断 IPv4 是否为内网地址
     *
     * <ul>
     *     <li>10.0.0.0 ~ 10.255.255.255</li>
     *     <li>172.16.0.0 ~ 172.31.255.255</li>
     *     <li>192.168.0.0 ~ 192.168.255.255</li>
     *     <li>127.0.0.1 视为内网</li>
     * </ul>
     *
     * @param ip IPv4 地址
     * @return 是否为内网 IPv4
     */
    public static boolean isInnerIpv4(String ip) {
        if (!isIpv4(ip)) {
            return false;
        }

        // 环回地址
        if (ip.startsWith("127.")) {
            return true;
        }

        // 内网段判断
        if (ip.startsWith("10.")) {
            return true;
        }
        if (ip.startsWith("192.168.")) {
            return true;
        }
        return ip.matches("^172\\.(1[6-9]|2\\d|3[0-1])\\..*");
    }

    /**
     * 判断 IPv6 是否为内网地址
     *
     * <ul>
     *     <li>环回地址 (::1)</li>
     *     <li>链路本地地址 (fe80::/10)</li>
     *     <li>唯一本地地址 (fc00::/7)</li>
     *     <li>通配符地址 (::)</li>
     * </ul>
     *
     * @param ip IPv6 地址
     * @return 是否为内网 IPv6
     */
    public static boolean isInnerIpv6(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            if (!(address instanceof Inet6Address inet6)) {
                return false;
            }

            // 环回地址 / 链路本地 / 唯一本地 / 通配符
            return inet6.isLoopbackAddress()
                    || inet6.isLinkLocalAddress()
                    || inet6.isSiteLocalAddress()
                    || inet6.isAnyLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 判断是否为内网地址（支持 IPv4 / IPv6）
     *
     * @param ip IP 地址
     * @return 是否为内网
     */
    public static boolean isInnerIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }

        if (isIpv4(ip)) {
            return isInnerIpv4(ip);
        } else if (isIpv6(ip)) {
            return isInnerIpv6(ip);
        }
        return false;
    }

    /**
     * 获取 IP 地址类型描述
     *
     * @param ip IP 地址
     * @return IPv4 / IPv6 / Unknown
     */
    public static IpTypeEnum ipType(String ip) {
        if (StringUtils.isBlank(ip)) {
            return IpTypeEnum.UNKNOWN;
        }
        if (isIpv4(ip)) {
            return IpTypeEnum.IPV4;
        }
        if (isIpv6(ip)) {
            return IpTypeEnum.IPV6;
        }
        return IpTypeEnum.UNKNOWN;
    }

    /**
     * 验证 IP 地址是否有效
     *
     * @param ip IP 地址
     * @return 是否有效
     */
    public static boolean isValidIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        ip = ip.trim();
        // 排除 unknown 和 localhost
        if ("unknown".equalsIgnoreCase(ip) || "localhost".equalsIgnoreCase(ip)) {
            return false;
        }
        // 基本格式检查：IPv4 或 IPv6
        return ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") ||
                ip.matches("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$") ||
                ip.matches("^::1$") ||
                ip.matches("^127\\.0\\.0\\.1$");
    }

    /**
     * 获取本机网卡IP地址，规则如下：
     *
     * <pre>
     * 1. 查找所有网卡地址，必须非回路（loopback）地址、非局域网地址（siteLocal）、IPv4地址
     * 2. 如果无满足要求的地址，调用 {@link InetAddress#getLocalHost()} 获取地址
     * </pre>
     *
     * <p>此方法不会抛出异常，获取失败将返回 {@code null}</p>
     *
     * @return 本机网卡IP地址，获取失败返回 {@code null}
     */
    public static InetAddress getLocalhost() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    // 必须不是回路地址、不是 siteLocal、IPv4
                    if (!address.isLoopbackAddress()
                            && !address.isSiteLocalAddress()
                            && address instanceof Inet4Address) {

                        return address;
                    }
                }
            }

            return fallback();
        } catch (Throwable _) {
            return null;
        }
    }

    private static InetAddress fallback() {
        try {
            return InetAddress.getLocalHost();
        } catch (Throwable _) {
            return null;
        }
    }

}
