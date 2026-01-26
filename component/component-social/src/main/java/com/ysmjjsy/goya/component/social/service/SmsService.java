package com.ysmjjsy.goya.component.social.service;

import com.ysmjjsy.goya.component.core.constants.DefaultConst;
import com.ysmjjsy.goya.component.social.cache.SmsCheckCacheManager;
import com.ysmjjsy.goya.component.social.exception.SocialException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;

import java.util.LinkedHashMap;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 22:40
 */
@Slf4j
@RequiredArgsConstructor
public class SmsService {

    private final SmsCheckCacheManager smsCheckCacheManager;

    /**
     * 发送验证码
     *
     * @param phoneNumber 手机号
     * @return 结果
     */
    public boolean sendSms(String phoneNumber) {
        String code = smsCheckCacheManager.put(phoneNumber);
        boolean result;
        if (Boolean.TRUE.equals(smsCheckCacheManager.getSms().sandbox())) {
            result = true;
        } else {
            SmsBlend smsBlend = SmsFactory.getSmsBlend();
            LinkedHashMap<String, String> message = new LinkedHashMap<>();
            message.put(DefaultConst.STR_CODE, code);
            SmsResponse smsResponse = smsBlend.sendMessage(phoneNumber, smsCheckCacheManager.getSms().templateId(), message);
            log.debug("[Goya] |- component [social] SmsService |- handle sms send response [{}]", smsResponse);
            result = smsResponse.isSuccess();
        }

        return result;
    }

    /**
     * 校验验证码是否正确
     *
     * @param phoneNumber 手机号
     * @param code        验证码
     * @return 是否正确
     */
    public boolean verify(String phoneNumber, String code) {
        if (StringUtils.isAnyBlank(phoneNumber, code)) {
            throw new SocialException("params is null");
        }

        boolean checked = smsCheckCacheManager.check(phoneNumber, code);
        smsCheckCacheManager.evict(phoneNumber);
        return checked;
    }
}
