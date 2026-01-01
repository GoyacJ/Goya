package com.ysmjjsy.goya.component.social.service;

import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import com.ysmjjsy.goya.component.common.strategy.IStrategyExecute;
import com.ysmjjsy.goya.component.social.cache.SmsCheckCacheManager;
import com.ysmjjsy.goya.component.social.enums.SocialTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class SmsService implements IStrategyExecute<String, Boolean> {

    private final SmsCheckCacheManager smsCheckCacheManager;

    @Override
    public String mark() {
        return SocialTypeEnum.SMS.getCode();
    }

    @Override
    public void execute(String phone) {
        executeResp(phone);
    }

    @Override
    public Boolean executeResp(String phone) {
        String code = smsCheckCacheManager.put(phone);
        boolean result;
        if (Boolean.TRUE.equals(smsCheckCacheManager.getSms().sandbox())) {
            result = true;
        } else {
            SmsBlend smsBlend = SmsFactory.getSmsBlend();
            LinkedHashMap<String, String> message = new LinkedHashMap<>();
            message.put(IBaseConstants.STR_CODE, code);
            SmsResponse smsResponse = smsBlend.sendMessage(phone, smsCheckCacheManager.getSms().templateId(), message);
            log.debug("[Goya] |- component [social] SmsService |- handle sms send response [{}]", smsResponse);
            result = smsResponse.isSuccess();
        }

        return result;
    }
}
