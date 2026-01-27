package com.ysmjjsy.goya.component.captcha.renderer.graphic;

import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;
import com.ysmjjsy.goya.component.captcha.provider.ResourceProvider;

/**
 * <p>Gif 类型验证码绘制器 </p>
 *
 * @author goya
 * @since 2021/12/20 22:54
 */
public class SpecGifCaptchaRenderer extends AbstractGifGraphicRenderer {

    public SpecGifCaptchaRenderer(ResourceProvider resourceProvider, CaptchaProperties captchaProperties) {
        super(resourceProvider,captchaProperties.graphics().expire());
    }

    @Override
    public CaptchaCategoryEnum getCategory() {
        return CaptchaCategoryEnum.SPEC_GIF;
    }

    @Override
    protected String[] getDrawCharacters() {
        return this.getCharCharacters();
    }
}
