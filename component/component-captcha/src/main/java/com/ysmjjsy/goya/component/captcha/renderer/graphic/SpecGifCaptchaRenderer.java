package com.ysmjjsy.goya.component.captcha.renderer.graphic;

import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;

/**
 * <p>Gif 类型验证码绘制器 </p>
 *
 * @author goya
 * @since 2021/12/20 22:54
 */
public class SpecGifCaptchaRenderer extends AbstractGifGraphicRenderer {

    @Override
    public CaptchaCategoryEnum getCategory() {
        return CaptchaCategoryEnum.SPEC_GIF;
    }

    @Override
    protected String[] getDrawCharacters() {
        return this.getCharCharacters();
    }
}
