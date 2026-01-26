package com.ysmjjsy.goya.component.captcha.core;

import com.ysmjjsy.goya.component.captcha.api.CaptchaService;
import com.ysmjjsy.goya.component.captcha.definition.AbstractCaptcha;
import com.ysmjjsy.goya.component.captcha.definition.Verification;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;
import com.ysmjjsy.goya.component.captcha.factory.CaptchaRendererFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/12 21:06
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultCaptchaManager implements CaptchaService {

    private final CaptchaRendererFactory captchaRendererFactory;

    @Override
    public AbstractCaptcha getCaptcha(String identity, CaptchaCategoryEnum category) {
        return captchaRendererFactory.getCaptcha(identity, category);
    }

    @Override
    public boolean verify(Verification verification) {
        return captchaRendererFactory.verify(verification);
    }
}
