package com.ysmjjsy.goya.component.captcha.renderer.graphic;

import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;

import java.awt.*;

/**
 * <p>中文类型验证码绘制器 </p>
 *
 * @author goya
 * @since 2021/12/20 22:55
 */
public class ChineseCaptchaRenderer extends AbstractPngGraphicRenderer {

    @Override
    public CaptchaCategoryEnum getCategory() {
        return CaptchaCategoryEnum.CHINESE;
    }

    @Override
    protected String[] getDrawCharacters() {
        return this.getWordCharacters();
    }

    @Override
    protected Font getFont() {
        return this.getResourceProvider().getChineseFont();
    }
}
