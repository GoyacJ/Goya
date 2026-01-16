package com.ysmjjsy.goya.component.social.service;

import cn.hutool.core.util.EnumUtil;
import com.ysmjjsy.goya.component.core.stragegy.StrategyExecute;
import com.ysmjjsy.goya.component.social.cache.ThirdPartCheckCacheManager;
import com.ysmjjsy.goya.component.social.exception.SocialException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.request.*;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 23:29
 */
@Slf4j
@RequiredArgsConstructor
public class ThirdPartService implements StrategyExecute<String, String> {

    private final ThirdPartCheckCacheManager thirdPartCheckCacheManager;

    @Override
    public String mark() {
        return "THIRDPART";
    }

    @Override
    public String executeResp(String resource) {
        return getAuthorizeUrl(resource);
    }

    public AuthRequest getAuthRequest(String source) {
        AuthDefaultSource authDefaultSource = parseAuthDefaultSource(source);
        AuthConfig authConfig = getAuthConfig(authDefaultSource);
        return getAuthRequest(authDefaultSource, authConfig);
    }

    public AuthRequest getAuthRequest(String source, AuthConfig authConfig) {
        AuthDefaultSource authDefaultSource = parseAuthDefaultSource(source);
        return getAuthRequest(authDefaultSource, authConfig);
    }

    /**
     * 返回带state参数的授权url，授权回调时会带上这个state
     *
     * @param source 第三方登录的类别 {@link AuthDefaultSource}
     * @return 返回授权地址
     */
    public String getAuthorizeUrl(String source) {
        AuthRequest authRequest = this.getAuthRequest(source);
        return authRequest.authorize(AuthStateUtils.createState());
    }

    public String getAuthorizeUrl(String source, AuthConfig authConfig) {
        AuthRequest authRequest = this.getAuthRequest(source, authConfig);
        return authRequest.authorize(AuthStateUtils.createState());
    }

    public Map<String, String> getAuthorizeUrls() {
        Map<String, AuthConfig> configs = getConfigs();
        return configs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> getAuthorizeUrl(entry.getKey(), entry.getValue())));
    }

    @NotNull
    private Map<String, AuthConfig> getConfigs() {
        Map<String, AuthConfig> configs = thirdPartCheckCacheManager.getThirdPart().configs();
        if (MapUtils.isEmpty(configs)) {
            throw new SocialException();
        }
        return configs;
    }


    @NotNull
    private AuthConfig getAuthConfig(AuthDefaultSource authDefaultSource) {
        Map<String, AuthConfig> configs = getConfigs();

        AuthConfig authConfig = configs.get(authDefaultSource.name());
        // 找不到对应关系，直接返回空
        if (ObjectUtils.isEmpty(authConfig)) {
            throw new SocialException();
        }
        return authConfig;
    }

    private static AuthDefaultSource parseAuthDefaultSource(String source) {
        AuthDefaultSource authDefaultSource;

        try {
            authDefaultSource = EnumUtil.fromString(AuthDefaultSource.class, source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SocialException();
        }
        return authDefaultSource;
    }

    private ThirdPartCheckCacheManager getThirdPartCheckCacheManager() {
        return thirdPartCheckCacheManager;
    }

    private AuthRequest getAuthRequest(AuthDefaultSource authDefaultSource, AuthConfig authConfig) {
        return switch (authDefaultSource) {
            case GITHUB -> new AuthGithubRequest(authConfig, this.getThirdPartCheckCacheManager());
            case WEIBO -> new AuthWeiboRequest(authConfig, this.getThirdPartCheckCacheManager());
            case GITEE -> new AuthGiteeRequest(authConfig, this.getThirdPartCheckCacheManager());
            case DINGTALK -> new AuthDingTalkRequest(authConfig, this.getThirdPartCheckCacheManager());
            case BAIDU -> new AuthBaiduRequest(authConfig, this.getThirdPartCheckCacheManager());
//            case CSDN -> new AuthCsdnRequest(authConfig, this.getJustAuthStateRedisCache());
            case CODING -> new AuthCodingRequest(authConfig, this.getThirdPartCheckCacheManager());
            case OSCHINA -> new AuthOschinaRequest(authConfig, this.getThirdPartCheckCacheManager());
            case ALIPAY ->
                    new AuthAlipayRequest(authConfig, thirdPartCheckCacheManager.getThirdPart().alipayPublicKey(), this.getThirdPartCheckCacheManager());
            case QQ -> new AuthQqRequest(authConfig, this.getThirdPartCheckCacheManager());
            case WECHAT_MP -> new AuthWeChatMpRequest(authConfig, this.getThirdPartCheckCacheManager());
            case WECHAT_OPEN -> new AuthWeChatOpenRequest(authConfig, this.getThirdPartCheckCacheManager());
            case WECHAT_ENTERPRISE ->
                    new AuthWeChatEnterpriseQrcodeRequest(authConfig, this.getThirdPartCheckCacheManager());
            case WECHAT_ENTERPRISE_WEB ->
                    new AuthWeChatEnterpriseWebRequest(authConfig, this.getThirdPartCheckCacheManager());
            case TAOBAO -> new AuthTaobaoRequest(authConfig, this.getThirdPartCheckCacheManager());
            case GOOGLE -> new AuthGoogleRequest(authConfig, this.getThirdPartCheckCacheManager());
            case FACEBOOK -> new AuthFacebookRequest(authConfig, this.getThirdPartCheckCacheManager());
            case DOUYIN -> new AuthDouyinRequest(authConfig, this.getThirdPartCheckCacheManager());
            case LINKEDIN -> new AuthLinkedinRequest(authConfig, this.getThirdPartCheckCacheManager());
            case MICROSOFT -> new AuthMicrosoftRequest(authConfig, this.getThirdPartCheckCacheManager());
            case MI -> new AuthMiRequest(authConfig, this.getThirdPartCheckCacheManager());
            case TOUTIAO -> new AuthToutiaoRequest(authConfig, this.getThirdPartCheckCacheManager());
            case TEAMBITION -> new AuthTeambitionRequest(authConfig, this.getThirdPartCheckCacheManager());
            case RENREN -> new AuthRenrenRequest(authConfig, this.getThirdPartCheckCacheManager());
            case PINTEREST -> new AuthPinterestRequest(authConfig, this.getThirdPartCheckCacheManager());
            case STACK_OVERFLOW -> new AuthStackOverflowRequest(authConfig, this.getThirdPartCheckCacheManager());
            case HUAWEI_V3 -> new AuthHuaweiV3Request(authConfig, this.getThirdPartCheckCacheManager());
            case GITLAB -> new AuthGitlabRequest(authConfig, this.getThirdPartCheckCacheManager());
            case KUJIALE -> new AuthKujialeRequest(authConfig, this.getThirdPartCheckCacheManager());
            case ELEME -> new AuthElemeRequest(authConfig, this.getThirdPartCheckCacheManager());
            case MEITUAN -> new AuthMeituanRequest(authConfig, this.getThirdPartCheckCacheManager());
            case TWITTER -> new AuthTwitterRequest(authConfig, this.getThirdPartCheckCacheManager());
            case FEISHU -> new AuthFeishuRequest(authConfig, this.getThirdPartCheckCacheManager());
            case JD -> new AuthJdRequest(authConfig, this.getThirdPartCheckCacheManager());
            case ALIYUN -> new AuthAliyunRequest(authConfig, this.getThirdPartCheckCacheManager());
            case XMLY -> new AuthXmlyRequest(authConfig, this.getThirdPartCheckCacheManager());
            default -> null;
        };
    }
}
