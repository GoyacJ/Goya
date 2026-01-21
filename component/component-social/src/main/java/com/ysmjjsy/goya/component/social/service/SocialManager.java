package com.ysmjjsy.goya.component.social.service;

import com.ysmjjsy.goya.component.social.domain.SocialUser;
import com.ysmjjsy.goya.component.social.domain.ThirdPrincipal;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 23:31
 */
public interface SocialManager {

    /**
     * 根据手机号获取 SocialUser
     *
     * @param phoneNumber 手机号
     * @return SocialUser
     */
    SocialUser loadSmsSocialUser(String phoneNumber);

    /**
     * 根据手机号获取并保存 SocialUser
     *
     * @param phoneNumber 手机号
     * @return SocialUser
     */
    SocialUser loadAndSaveSmsSocialUser(String phoneNumber);

    /**
     * 根据source+principal获取 SocialUser
     *
     * @param source    source
     * @param principal principal
     * @return SocialUser
     */
    SocialUser loadThirdSocialUser(String source, ThirdPrincipal principal);

    /**
     * 根据source+principal获取并保存 SocialUser
     *
     * @param source    source
     * @param principal principal
     * @return SocialUser
     */
    SocialUser loadAndSaveThirdSocialUser(String source, ThirdPrincipal principal);

    /**
     * 根据openId+unionId获取 SocialUser
     *
     * @param openId        openId
     * @param appId         appId
     * @param sessionKey    sessionKey
     * @param encryptedData encryptedData
     * @param iv            iv
     * @return SocialUser
     */
    SocialUser loadWxAppSocialUser(String openId, String appId, String sessionKey, String encryptedData, String iv);

    /**
     * 根据openId+unionId获取并保存 SocialUser
     *
     * @param openId        openId
     * @param appId         appId
     * @param sessionKey    sessionKey
     * @param encryptedData encryptedData
     * @param iv            iv
     * @return SocialUser
     */
    SocialUser loadAndSaveWxAppSocialUser(String openId, String appId, String sessionKey, String encryptedData, String iv);
}
