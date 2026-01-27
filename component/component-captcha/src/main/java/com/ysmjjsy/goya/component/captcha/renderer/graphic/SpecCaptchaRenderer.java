package com.ysmjjsy.goya.component.captcha.renderer.graphic;

import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;
import com.ysmjjsy.goya.component.captcha.provider.ResourceProvider;

/**
 * <p>类型验证码绘制器 </p>
 *
 * @author goya
 * @since 2021/12/20 20:39
 */
public class SpecCaptchaRenderer extends AbstractPngGraphicRenderer {

    public SpecCaptchaRenderer(ResourceProvider resourceProvider, CaptchaProperties captchaProperties) {
        super(resourceProvider,captchaProperties.graphics().expire());
    }

    @Override
    public CaptchaCategoryEnum getCategory() {
        return CaptchaCategoryEnum.SPEC;
    }

    @Override
    protected String[] getDrawCharacters() {
        return this.getCharCharacters();
    }
}
