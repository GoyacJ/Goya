package com.ysmjjsy.goya.component.security.core.enums;

/**
 * <p>客户端类型枚举</p>
 * <p>用于区分不同客户端的认证流程</p>
 *
 * @author goya
 * @since 2026/2/4
 */
public enum ClientTypeEnum {
    /**
     * Web 浏览器
     * 特点：支持 Session、Cookie、SSO
     */
    WEB,

    /**
     * 移动端 App（iOS/Android）
     * 特点：不支持 Session，使用临时 Token + Redis 存储授权状态
     */
    MOBILE_APP,

    /**
     * 微信小程序
     * 特点：不支持 Session，直接返回 Token
     */
    MINIPROGRAM,

    /**
     * 桌面应用（可选）
     * 特点：类似移动端，不支持 Session
     */
    DESKTOP_APP
}
