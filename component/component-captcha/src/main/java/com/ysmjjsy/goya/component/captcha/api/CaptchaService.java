package com.ysmjjsy.goya.component.captcha.api;

import com.ysmjjsy.goya.component.captcha.definition.AbstractCaptcha;
import com.ysmjjsy.goya.component.captcha.definition.Verification;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/12 21:05
 */
public interface CaptchaService {

    /**
     * 获取验证码
     * @param identity 身份标识
     * @param category 验证码类别
     * @return 验证码信息
     */
    AbstractCaptcha getCaptcha(String identity, CaptchaCategoryEnum category);

    /**
     * 校验验证码
     * @param verification 验证码信息
     * @return 校验结果
     */
    boolean verify(Verification verification);
}
