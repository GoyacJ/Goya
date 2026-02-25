package com.ysmjjsy.goya.component.social.service;

import com.ysmjjsy.goya.component.framework.cache.support.CacheSupport;
import com.ysmjjsy.goya.component.framework.core.json.GoyaJson;
import com.ysmjjsy.goya.component.social.configuration.properties.SocialProperties;
import com.ysmjjsy.goya.component.social.constants.ISocialConstants;
import com.ysmjjsy.goya.component.social.domain.SocialUser;
import com.ysmjjsy.goya.component.social.enums.SocialTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>基于缓存的社交绑定存储默认实现</p>
 *
 * @author goya
 * @since 2026/2/25
 */
public class CacheSocialBindingStore extends CacheSupport<String, String> implements SocialBindingStore {

    private static final String USER_KEY_PREFIX = "user:";
    private static final String PHONE_KEY_PREFIX = "phone:";
    private static final String THIRD_KEY_PREFIX = "third:";
    private static final String WX_OPEN_KEY_PREFIX = "wx:open:";
    private static final String WX_UNION_KEY_PREFIX = "wx:union:";
    private static final String KEY_SEPARATOR = ":";
    private static final Duration DEFAULT_EXPIRE = Duration.ofDays(3650);

    private final Duration bindingExpire;

    public CacheSocialBindingStore(SocialProperties.Binding binding) {
        super(ISocialConstants.CACHE_SOCIAL_BINDING, resolveExpire(binding));
        this.bindingExpire = resolveExpire(binding);
    }

    @Override
    public SocialUser findByPhone(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            return null;
        }
        return findByIndexKey(phoneIndexKey(phoneNumber));
    }

    @Override
    public SocialUser findByThird(String source, String uuid) {
        if (StringUtils.isAnyBlank(source, uuid)) {
            return null;
        }
        return findByIndexKey(thirdIndexKey(source, uuid));
    }

    @Override
    public SocialUser findByWxMiniProgram(String openId, String unionId, String appId) {
        if (StringUtils.isBlank(appId)) {
            return null;
        }
        if (StringUtils.isNotBlank(openId)) {
            SocialUser byOpenId = findByIndexKey(wxOpenIndexKey(appId, openId));
            if (byOpenId != null) {
                return byOpenId;
            }
        }
        if (StringUtils.isNotBlank(unionId)) {
            return findByIndexKey(wxUnionIndexKey(appId, unionId));
        }
        return null;
    }

    @Override
    public SocialUser saveByPhone(String phoneNumber) {
        SocialUser existing = findByPhone(phoneNumber);
        if (existing != null) {
            return existing;
        }
        return SocialUser.builder()
                .phoneNumber(phoneNumber)
                .source(SocialTypeEnum.SMS.getCode())
                .build();
    }

    @Override
    public SocialUser save(SocialUser socialUser) {
        return persist(null, socialUser);
    }

    @Override
    public SocialUser update(String userId, SocialUser socialUser) {
        return persist(userId, socialUser);
    }

    private SocialUser persist(String userId, SocialUser socialUser) {
        if (socialUser == null) {
            return null;
        }
        String finalUserId = StringUtils.defaultIfBlank(userId, socialUser.getUserId());
        if (StringUtils.isBlank(finalUserId)) {
            return socialUser;
        }
        socialUser.setUserId(finalUserId);

        SocialUser existing = findByUserId(finalUserId);
        cleanupStaleIndexes(existing, socialUser);

        put(userKey(finalUserId), serialize(socialUser), bindingExpire);
        for (String indexKey : collectIndexKeys(socialUser)) {
            put(indexKey, finalUserId, bindingExpire);
        }
        return socialUser;
    }

    private void cleanupStaleIndexes(SocialUser existing, SocialUser target) {
        if (existing == null) {
            return;
        }
        Set<String> oldKeys = collectIndexKeys(existing);
        Set<String> newKeys = collectIndexKeys(target);
        for (String oldKey : oldKeys) {
            if (!newKeys.contains(oldKey)) {
                delete(oldKey);
            }
        }
    }

    private Set<String> collectIndexKeys(SocialUser socialUser) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        if (socialUser == null) {
            return keys;
        }
        if (StringUtils.isNotBlank(socialUser.getPhoneNumber())) {
            keys.add(phoneIndexKey(socialUser.getPhoneNumber()));
        }
        if (StringUtils.isNotBlank(socialUser.getSource()) && StringUtils.isNotBlank(socialUser.getUuid())) {
            keys.add(thirdIndexKey(socialUser.getSource(), socialUser.getUuid()));
        }
        if (StringUtils.isNotBlank(socialUser.getAppId()) && StringUtils.isNotBlank(socialUser.getOpenId())) {
            keys.add(wxOpenIndexKey(socialUser.getAppId(), socialUser.getOpenId()));
        }
        if (StringUtils.isNotBlank(socialUser.getAppId()) && StringUtils.isNotBlank(socialUser.getUnionId())) {
            keys.add(wxUnionIndexKey(socialUser.getAppId(), socialUser.getUnionId()));
        }
        return keys;
    }

    private SocialUser findByIndexKey(String indexKey) {
        if (StringUtils.isBlank(indexKey)) {
            return null;
        }
        String userId = get(indexKey);
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return findByUserId(userId);
    }

    private SocialUser findByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        String payload = get(userKey(userId));
        if (StringUtils.isBlank(payload)) {
            return SocialUser.builder().userId(userId).build();
        }
        SocialUser socialUser = GoyaJson.fromJson(payload, SocialUser.class);
        if (socialUser == null) {
            return SocialUser.builder().userId(userId).build();
        }
        if (StringUtils.isBlank(socialUser.getUserId())) {
            socialUser.setUserId(userId);
        }
        return socialUser;
    }

    private String serialize(SocialUser socialUser) {
        return GoyaJson.toJson(socialUser);
    }

    private String userKey(String userId) {
        return USER_KEY_PREFIX + userId.trim();
    }

    private String phoneIndexKey(String phoneNumber) {
        return PHONE_KEY_PREFIX + phoneNumber.trim();
    }

    private String thirdIndexKey(String source, String uuid) {
        return THIRD_KEY_PREFIX
                + source.trim().toLowerCase()
                + KEY_SEPARATOR
                + uuid.trim();
    }

    private String wxOpenIndexKey(String appId, String openId) {
        return WX_OPEN_KEY_PREFIX
                + appId.trim().toLowerCase()
                + KEY_SEPARATOR
                + openId.trim();
    }

    private String wxUnionIndexKey(String appId, String unionId) {
        return WX_UNION_KEY_PREFIX
                + appId.trim().toLowerCase()
                + KEY_SEPARATOR
                + unionId.trim();
    }

    private static Duration resolveExpire(SocialProperties.Binding binding) {
        if (binding == null || binding.expire() == null || binding.expire().isNegative() || binding.expire().isZero()) {
            return DEFAULT_EXPIRE;
        }
        return binding.expire();
    }
}
