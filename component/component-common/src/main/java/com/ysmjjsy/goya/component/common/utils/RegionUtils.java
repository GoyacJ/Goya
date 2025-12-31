package com.ysmjjsy.goya.component.common.utils;

import com.ysmjjsy.goya.component.common.exceptions.CommonException;
import com.ysmjjsy.goya.component.common.utils.HtmlUtils;
import com.ysmjjsy.goya.component.common.utils.ResourceUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.xdb.Searcher;

/**
 * <p>根据ip地址定位工具类，离线方式</p>
 *
 * @author goya
 * @since 2025/10/14 17:35
 */
@Slf4j
@UtilityClass
public class RegionUtils {

    /**
     * IP地址库文件名称
     */
    public static final String IP_XDB_FILENAME = "db/ip2region.xdb";

    /**
     * Searcher
     */
    private static final Searcher SEARCHER;

    /**
     * 未知IP
     */
    public static final String UNKNOWN_IP = "XX XX";

    /**
     * 内网地址
     */
    public static final String LOCAL_ADDRESS = "内网IP";

    /**
     * 未知地址
     */
    public static final String UNKNOWN_ADDRESS = "未知";

    static {
        try {
            // 1、将 ip2region 数据库文件 xdb 从 ClassPath 加载到内存。
            // 2、基于加载到内存的 xdb 数据创建一个 Searcher 查询对象。
            SEARCHER = Searcher.newWithBuffer(ResourceUtils.readBytes(IP_XDB_FILENAME));
            log.info("RegionUtils初始化成功，加载IP地址库数据成功！");
        } catch (Exception e) {
            throw new CommonException("RegionUtils初始化失败，原因：" + e.getMessage());
        }
    }

    public static String getRealAddressByIp(String ip) {
        if (StringUtils.isBlank(ip)){
            ip = "";
        }
        // 处理空串并过滤HTML标签
        ip = HtmlUtils.cleanHtmlTag(ip);
        // 判断是否为IPv4
        if (NetUtils.isIpv4(ip)) {
            return resolverIpv4Region(ip);
        }
        // 判断是否为IPv6
        if (NetUtils.isIpv6(ip)) {
            return resolverIpv6Region(ip);
        }
        // 如果不是IPv4或IPv6，则返回未知IP
        return UNKNOWN_IP;
    }

    /**
     * 根据IPv4地址查询IP归属行政区域
     * @param ip ipv4地址
     * @return 归属行政区域
     */
    private static String resolverIpv4Region(String ip){
        // 内网不查询
        if (NetUtils.isInnerIpv4(ip)) {
            return LOCAL_ADDRESS;
        }
        return RegionUtils.getCityInfo(ip);
    }

    /**
     * 根据IPv6地址查询IP归属行政区域
     * @param ip ipv6地址
     * @return 归属行政区域
     */
    private static String resolverIpv6Region(String ip){
        // 内网不查询
        if (NetUtils.isInnerIpv6(ip)) {
            return LOCAL_ADDRESS;
        }
        log.warn("ip2region不支持IPV6地址解析：{}", ip);
        // 不支持IPv6，不再进行没有必要的IP地址信息的解析，直接返回
        // 如有需要，可自行实现IPv6地址信息解析逻辑，并在这里返回
        return UNKNOWN_ADDRESS;
    }

    /**
     * 根据IP地址离线获取城市
     */
    public static String getCityInfo(String ip) {
        try {
            // 3、执行查询
            String region = SEARCHER.search(StringUtils.trim(ip));
            return region.replace("0|", "").replace("|0", "");
        } catch (Exception e) {
            log.error("IP地址离线获取城市异常 {}", ip);
            return "未知";
        }
    }
}
