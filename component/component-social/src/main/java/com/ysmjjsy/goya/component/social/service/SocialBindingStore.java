package com.ysmjjsy.goya.component.social.service;

import com.ysmjjsy.goya.component.social.domain.SocialUser;

/**
 * <p>社交账号绑定存储</p>
 *
 * @author goya
 * @since 2026/2/25
 */
public interface SocialBindingStore {

    /**
     * 按手机号查找绑定用户。
     *
     * @param phoneNumber 手机号
     * @return 社交用户
     */
    SocialUser findByPhone(String phoneNumber);

    /**
     * 按第三方来源与UUID查找绑定用户。
     *
     * @param source 第三方来源
     * @param uuid   第三方UUID
     * @return 社交用户
     */
    SocialUser findByThird(String source, String uuid);

    /**
     * 按微信小程序标识查找绑定用户。
     *
     * @param openId  openId
     * @param unionId unionId
     * @param appId   appId
     * @return 社交用户
     */
    SocialUser findByWxMiniProgram(String openId, String unionId, String appId);

    /**
     * 按手机号保存或初始化绑定对象。
     *
     * @param phoneNumber 手机号
     * @return 社交用户
     */
    SocialUser saveByPhone(String phoneNumber);

    /**
     * 保存绑定信息。
     *
     * @param socialUser 社交用户
     * @return 保存后的社交用户
     */
    SocialUser save(SocialUser socialUser);

    /**
     * 更新绑定信息。
     *
     * @param userId     用户ID
     * @param socialUser 社交用户
     * @return 更新后的社交用户
     */
    SocialUser update(String userId, SocialUser socialUser);
}
