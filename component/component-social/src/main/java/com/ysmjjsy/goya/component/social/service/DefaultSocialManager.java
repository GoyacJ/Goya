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

    public DefaultSocialManager(ThirdPartService thirdPartService, WxMiniProgramService wxMiniProgramService) {
        super(thirdPartService, wxMiniProgramService);
    }

    @Override
    SocialUser findUserByPhone(String phoneNumber) {
        return null;
    }

    @Override
    SocialUser saveSocialUserByPhone(String phoneNumber) {
        return null;
    }

    @Override
    SocialUser findUserByThird(String source, String uuid) {
        return null;
    }

    @Override
    SocialUser saveSocialUser(SocialUser socialUser) {
        return socialUser;
    }

    @Override
    SocialUser updateSocialUser(String userId, SocialUser socialUser) {
        return socialUser;
    }

    @Override
    SocialUser findUserByWxMiniProgram(String openId, String unionId, String appId) {
        return null;
    }
}
