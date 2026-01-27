package com.ysmjjsy.goya.component.captcha.renderer.graphic;

import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;
import com.ysmjjsy.goya.component.captcha.provider.ResourceProvider;

import java.awt.*;

/**
 * <p>中文Gif类型验证码绘制器 </p>
 *
 * @author goya
 * @since 2021/12/20 22:55
 */
public class ChineseGifCaptchaRenderer extends AbstractGifGraphicRenderer {

    public ChineseGifCaptchaRenderer(ResourceProvider resourceProvider, CaptchaProperties captchaProperties) {
        super(resourceProvider,captchaProperties.graphics().expire());
    }

    @Override
    public CaptchaCategoryEnum getCategory() {
        return CaptchaCategoryEnum.CHINESE_GIF;
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
