package com.ysmjjsy.goya.component.social.service;

import com.ysmjjsy.goya.component.framework.strategy.StrategyChoose;
import com.ysmjjsy.goya.component.social.enums.SocialTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 23:31
 */
@Slf4j
@RequiredArgsConstructor
public class SocialService {

    private final StrategyChoose strategyChoose;
    private final SmsService smsService;
    private final ThirdPartService thirdPartService;
    private final WxMiniProgramService wxMiniProgramService;

    /**
     * 策略执行
     * @param request 请求参数
     * @param socialType 社交类型
     * @return 响应结果
     * @param <I> 请求参数类型
     * @param <O> 响应结果类型
     */
    public <I, O> O execute(I request, SocialTypeEnum socialType) {
        return strategyChoose.chooseAndExecuteResp(socialType.getMark(), request);
    }

    /**
     * 校验验证码是否正确
     *
     * @param phoneNumber 手机号
     * @param code        验证码
     * @return 是否正确
     */
    public boolean verify(String phoneNumber, String code) {
        return smsService.verify(phoneNumber,code);
    }
}
