package com.ysmjjsy.goya.component.social.service;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.ysmjjsy.goya.component.framework.common.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaStringUtils;
import com.ysmjjsy.goya.component.social.domain.SocialUser;
import com.ysmjjsy.goya.component.social.domain.ThirdPrincipal;
import com.ysmjjsy.goya.component.social.enums.GenderEnum;
import com.ysmjjsy.goya.component.social.enums.SocialTypeEnum;
import com.ysmjjsy.goya.component.social.exception.SocialException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthUser;

import java.util.Objects;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/20 23:59
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSocialManager implements SocialManager {

    private final ThirdPartService thirdPartService;
    private final WxMiniProgramService wxMiniProgramService;

    /**
     * 根据手机号加载用户
     *
     * @param phoneNumber 手机号
     * @return SocialUser
     */
    @Override
    public SocialUser loadSmsSocialUser(String phoneNumber) {
        if (GoyaStringUtils.isEmpty(phoneNumber)) {
            throw new SocialException("phone number is null");
        }

        SocialUser socialUser = SocialUser.builder().phoneNumber(phoneNumber).source(SocialTypeEnum.SMS.getCode()).build();

        SocialUser userByPhone = findUserByPhone(phoneNumber);
        if (Objects.nonNull(userByPhone)) {
            return userByPhone;
        }
        return socialUser;
    }

    /**
     * 根据手机号加载用户并保存用户信息
     *
     * @param phoneNumber 手机号
     * @return SocialUser
     */
    @Override
    public SocialUser loadAndSaveSmsSocialUser(String phoneNumber) {
        SocialUser socialUser = loadSmsSocialUser(phoneNumber);
        if (GoyaStringUtils.isEmpty(socialUser.getUserId())) {
            return saveSocialUserByPhone(phoneNumber);
        }
        return socialUser;
    }

    @Override
    public SocialUser loadThirdSocialUser(String source, ThirdPrincipal principal) {
        AuthCallback authCallback = AuthCallback.builder()
                .code(principal.getCode())
                .auth_code(principal.getAuth_code())
                .state(principal.getState())
                .authorization_code(principal.getAuthorization_code())
                .oauth_token(principal.getOauth_token())
                .oauth_verifier(principal.getOauth_verifier())
                .build();

        AuthUser authUser = thirdPartService.login(source, authCallback);

        SocialUser userByThird = findUserByThird(authUser.getSource(), authUser.getUuid());
        if (Objects.nonNull(userByThird)) {
            return convertAuthUserToSocialUser(authUser, userByThird);
        }
        return convertAuthUserToSocialUser(authUser, null);
    }

    @Override
    public SocialUser loadAndSaveThirdSocialUser(String source, ThirdPrincipal principal) {
        SocialUser socialUser = loadThirdSocialUser(source, principal);
        if (GoyaStringUtils.isEmpty(socialUser.getUserId())) {
            return saveSocialUser(socialUser);
        }
        return updateSocialUser(socialUser.getUserId(), socialUser);
    }

    private SocialUser convertAuthUserToSocialUser(AuthUser authUser, SocialUser socialUser) {
        if (Objects.isNull(authUser)) {
            return null;
        }
        SocialUser.SocialUserBuilder builder = SocialUser.builder()
                .socialTypeEnum(SocialTypeEnum.THIRD_PART)
                .uuid(authUser.getUuid())
                .username(authUser.getUsername())
                .nickname(authUser.getNickname())
                .avatar(authUser.getAvatar())
                .blog(authUser.getBlog())
                .company(authUser.getCompany())
                .location(authUser.getLocation())
                .email(authUser.getEmail())
                .remark(authUser.getRemark())
                .gender(GenderEnum.convert(authUser.getGender()));
        if (Objects.nonNull(authUser.getToken())) {
            builder.accessToken(authUser.getToken().getAccessToken())
                    .expireIn(authUser.getToken().getExpireIn())
                    .refreshToken(authUser.getToken().getRefreshToken())
                    .refreshTokenExpireIn(authUser.getToken().getRefreshTokenExpireIn())
                    .scope(authUser.getToken().getScope())
                    .tokenType(authUser.getToken().getTokenType())
                    .uid(authUser.getToken().getUid())
                    .openId(authUser.getToken().getOpenId())
                    .accessCode(authUser.getToken().getAccessCode())
                    .unionId(authUser.getToken().getUnionId());
        }

        if (Objects.nonNull(socialUser)) {
            builder.userId(socialUser.getUserId())
                    .name(socialUser.getName());
        }
        return builder.build();
    }

    @Override
    public SocialUser loadWxAppSocialUser(String openId, String appId, String sessionKey, String encryptedData, String iv) {
        WxMaUserInfo userInfo = wxMiniProgramService.getUserInfo(appId, sessionKey, encryptedData, iv);
        SocialUser userByWxMiniProgram = findUserByWxMiniProgram(openId, userInfo.getUnionId(), appId);
        if (Objects.nonNull(userByWxMiniProgram)) {
            return convertAuthUserToSocialUser(openId, appId, userInfo, userByWxMiniProgram);
        }
        return convertAuthUserToSocialUser(openId, appId, userInfo, null);
    }

    @Override
    public SocialUser loadAndSaveWxAppSocialUser(String openId, String appId, String sessionKey, String encryptedData, String iv) {
        SocialUser socialUser = loadWxAppSocialUser(openId, appId, sessionKey, encryptedData, iv);
        if (GoyaStringUtils.isEmpty(socialUser.getUserId())) {
            return saveSocialUser(socialUser);
        }
        return updateSocialUser(socialUser.getUserId(), socialUser);
    }

    private SocialUser convertAuthUserToSocialUser(String openId, String appId, WxMaUserInfo userInfo, SocialUser socialUser) {
        SocialUser.SocialUserBuilder builder = SocialUser
                .builder()
                .uuid(openId)
                .openId(openId)
                .nickname(userInfo.getNickName())
                .username(userInfo.getNickName())
                .avatar(userInfo.getAvatarUrl())
                .location(userInfo.getCountry() + SymbolConst.FORWARD_SLASH + userInfo.getProvince() + SymbolConst.FORWARD_SLASH + userInfo.getCity())
                .gender(GenderEnum.getByCode(userInfo.getGender()))
                .source(SocialTypeEnum.WECHAT_MINI_PROGRAM.getCode())
                .unionId(userInfo.getUnionId())
                .appId(appId);

        if (Objects.nonNull(socialUser)) {
            builder.userId(socialUser.getUserId())
                    .name(socialUser.getName());
        }

        return builder.build();
    }

    /**
     * 根据手机号查询用户
     *
     * @param phoneNumber 手机号
     * @return 用户
     */
    abstract SocialUser findUserByPhone(String phoneNumber);

    /**
     * 根据phoneNumber保存用户信息
     *
     * @return 用户信息
     */
    abstract SocialUser saveSocialUserByPhone(String phoneNumber);

    /**
     * 根据source+uuid查询用户
     *
     * @param source source
     * @param uuid   uuid
     * @return 用户
     */
    abstract SocialUser findUserByThird(String source, String uuid);

    /**
     * 根据socialUser保存用户信息
     *
     * @param socialUser socialUser
     * @return 用户信息
     */
    abstract SocialUser saveSocialUser(SocialUser socialUser);

    /**
     * 根据socialUser更新用户信息
     *
     * @param userId     用户Id
     * @param socialUser socialUser
     * @return 用户信息
     */
    abstract SocialUser updateSocialUser(String userId, SocialUser socialUser);

    /**
     * 根据source+uuid查询用户
     *
     * @param openId  openId
     * @param unionId unionId
     * @param appId   appId
     * @return 用户
     */
    abstract SocialUser findUserByWxMiniProgram(String openId, String unionId, String appId);
}
