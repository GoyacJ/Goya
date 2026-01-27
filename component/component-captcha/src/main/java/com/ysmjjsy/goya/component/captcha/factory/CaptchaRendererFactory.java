package com.ysmjjsy.goya.component.captcha.factory;

import com.ysmjjsy.goya.component.captcha.definition.AbstractCaptcha;
import com.ysmjjsy.goya.component.captcha.definition.ICaptchaRenderer;
import com.ysmjjsy.goya.component.captcha.definition.Verification;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;
import com.ysmjjsy.goya.component.captcha.exception.CaptchaErrorCode;
import com.ysmjjsy.goya.component.captcha.exception.CaptchaException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 14:43
 */
public class CaptchaRendererFactory {

    @Autowired
    private final Map<String, ICaptchaRenderer> handlers = new ConcurrentHashMap<>(8);

    public ICaptchaRenderer getRenderer(CaptchaCategoryEnum category) {
        ICaptchaRenderer captchaRenderer = handlers.get(category.getCode());
        if (ObjectUtils.isEmpty(captchaRenderer)) {
            throw new CaptchaException(CaptchaErrorCode.HANDLER_NOT_EXIST);
        }

        return captchaRenderer;
    }

    public AbstractCaptcha getCaptcha(String identity, CaptchaCategoryEnum category) {
        ICaptchaRenderer captchaRenderer = getRenderer(category);
        return captchaRenderer.getCaptcha(identity);
    }

    public boolean verify(Verification verification) {
        ICaptchaRenderer captchaRenderer = getRenderer(verification.getCategory());
        return captchaRenderer.verify(verification);
    }
}
