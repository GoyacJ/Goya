package com.ysmjjsy.goya.component.security.core.service;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * <p>第三方账号服务SPI</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface ISocialUserService {

    /**
     * 根据第三方账号信息解析并绑定本地用户
     *
     * @param provider 第三方来源（如 github、gitee）
     * @param attributes 第三方用户属性
     * @return 用户ID
     */
    @Nullable
    String resolveUserId(String provider, Map<String, Object> attributes);

    /**
     * 微信小程序登录用户解析
     *
     * @return 用户ID
     */
    @Nullable
    String resolveUserIdForWxApp(String openId,
                                 String appId,
                                 String sessionKey,
                                 String encryptedData,
                                 String iv);
}
