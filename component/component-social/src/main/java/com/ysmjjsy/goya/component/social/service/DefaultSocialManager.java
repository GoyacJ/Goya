package com.ysmjjsy.goya.component.social.service;

import com.ysmjjsy.goya.component.social.domain.SocialUser;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/21 00:00
 */
@Slf4j
public class DefaultSocialManager extends AbstractSocialManager {

    private final SocialBindingStore socialBindingStore;

    public DefaultSocialManager(ThirdPartService thirdPartService,
                                WxMiniProgramService wxMiniProgramService,
                                SocialBindingStore socialBindingStore) {
        super(thirdPartService, wxMiniProgramService);
        this.socialBindingStore = socialBindingStore;
    }

    @Override
    SocialUser findUserByPhone(String phoneNumber) {
        return socialBindingStore.findByPhone(phoneNumber);
    }

    @Override
    SocialUser saveSocialUserByPhone(String phoneNumber) {
        return socialBindingStore.saveByPhone(phoneNumber);
    }

    @Override
    SocialUser findUserByThird(String source, String uuid) {
        return socialBindingStore.findByThird(source, uuid);
    }

    @Override
    SocialUser saveSocialUser(SocialUser socialUser) {
        return socialBindingStore.save(socialUser);
    }

    @Override
    SocialUser updateSocialUser(String userId, SocialUser socialUser) {
        return socialBindingStore.update(userId, socialUser);
    }

    @Override
    SocialUser findUserByWxMiniProgram(String openId, String unionId, String appId) {
        return socialBindingStore.findByWxMiniProgram(openId, unionId, appId);
    }
}
