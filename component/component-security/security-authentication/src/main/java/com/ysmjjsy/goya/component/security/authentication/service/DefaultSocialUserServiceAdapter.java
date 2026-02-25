package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.security.core.service.ISocialUserService;
import com.ysmjjsy.goya.component.social.domain.SocialUser;
import com.ysmjjsy.goya.component.social.service.SocialBindingStore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * <p>默认社交用户 SPI 适配器</p>
 *
 * @author goya
 * @since 2026/2/25
 */
public class DefaultSocialUserServiceAdapter implements ISocialUserService {

    private static final String ATTRIBUTE_UUID = "uuid";
    private static final String ATTRIBUTE_OPEN_ID = "openId";
    private static final String ATTRIBUTE_OPENID = "openid";
    private static final String ATTRIBUTE_UNION_ID = "unionId";
    private static final String ATTRIBUTE_UNIONID = "unionid";
    private static final String ATTRIBUTE_APP_ID = "appId";
    private static final String ATTRIBUTE_APPID = "appid";
    private static final String ATTRIBUTE_PHONE = "phoneNumber";
    private static final String ATTRIBUTE_PHONE_ALT = "phone";

    private final ObjectProvider<SocialBindingStore> socialBindingStoreProvider;

    public DefaultSocialUserServiceAdapter(ObjectProvider<SocialBindingStore> socialBindingStoreProvider) {
        this.socialBindingStoreProvider = socialBindingStoreProvider;
    }

    @Override
    public String resolveUserId(String provider, Map<String, Object> attributes) {
        SocialBindingStore socialBindingStore = socialBindingStoreProvider.getIfAvailable();
        if (socialBindingStore == null || CollectionUtils.isEmpty(attributes)) {
            return null;
        }

        String uuid = firstNonBlank(
                readString(attributes, ATTRIBUTE_UUID),
                readString(attributes, ATTRIBUTE_OPEN_ID),
                readString(attributes, ATTRIBUTE_OPENID),
                readString(attributes, "id"),
                readString(attributes, "uid"),
                readString(attributes, "sub")
        );
        String source = firstNonBlank(provider, readString(attributes, "source"));
        if (StringUtils.isNotBlank(source) && StringUtils.isNotBlank(uuid)) {
            SocialUser socialUser = socialBindingStore.findByThird(source, uuid);
            if (socialUser != null && StringUtils.isNotBlank(socialUser.getUserId())) {
                return socialUser.getUserId();
            }
        }

        String phoneNumber = firstNonBlank(readString(attributes, ATTRIBUTE_PHONE), readString(attributes, ATTRIBUTE_PHONE_ALT));
        if (StringUtils.isNotBlank(phoneNumber)) {
            SocialUser socialUser = socialBindingStore.findByPhone(phoneNumber);
            if (socialUser != null && StringUtils.isNotBlank(socialUser.getUserId())) {
                return socialUser.getUserId();
            }
        }

        String openId = firstNonBlank(readString(attributes, ATTRIBUTE_OPEN_ID), readString(attributes, ATTRIBUTE_OPENID));
        String unionId = firstNonBlank(readString(attributes, ATTRIBUTE_UNION_ID), readString(attributes, ATTRIBUTE_UNIONID));
        String appId = firstNonBlank(readString(attributes, ATTRIBUTE_APP_ID), readString(attributes, ATTRIBUTE_APPID));
        if (StringUtils.isNotBlank(openId) && StringUtils.isNotBlank(appId)) {
            SocialUser socialUser = socialBindingStore.findByWxMiniProgram(openId, unionId, appId);
            if (socialUser != null && StringUtils.isNotBlank(socialUser.getUserId())) {
                return socialUser.getUserId();
            }
        }

        return null;
    }

    @Override
    public String resolveUserIdForWxApp(String openId, String appId, String sessionKey, String encryptedData, String iv) {
        SocialBindingStore socialBindingStore = socialBindingStoreProvider.getIfAvailable();
        if (socialBindingStore == null || StringUtils.isAnyBlank(openId, appId)) {
            return null;
        }
        SocialUser socialUser = socialBindingStore.findByWxMiniProgram(openId, null, appId);
        if (socialUser == null || StringUtils.isBlank(socialUser.getUserId())) {
            return null;
        }
        return socialUser.getUserId();
    }

    private String readString(Map<String, Object> attributes, String key) {
        if (attributes == null || StringUtils.isBlank(key)) {
            return null;
        }
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return StringUtils.isBlank(text) ? null : text;
    }

    private String firstNonBlank(String... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
}
