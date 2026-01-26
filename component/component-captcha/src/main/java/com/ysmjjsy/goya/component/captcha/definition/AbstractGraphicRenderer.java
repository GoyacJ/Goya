package com.ysmjjsy.goya.component.captcha.definition;

import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.constants.CaptchaConst;
import com.ysmjjsy.goya.component.captcha.exception.CaptchaHasExpiredException;
import com.ysmjjsy.goya.component.captcha.exception.CaptchaIsEmptyException;
import com.ysmjjsy.goya.component.captcha.exception.CaptchaMismatchException;
import com.ysmjjsy.goya.component.captcha.exception.CaptchaParameterIllegalException;
import com.ysmjjsy.goya.component.captcha.provider.ResourceProvider;
import com.ysmjjsy.goya.component.core.utils.GoyaIdUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.awt.*;

/**
 * <p>抽象的图形验证码</p>
 *
 * @author goya
 * @since 2025/9/30 16:15
 */
public abstract class AbstractGraphicRenderer extends AbstractRenderer<String, String> {

    private GraphicCaptcha graphicCaptcha;

    protected AbstractGraphicRenderer(ResourceProvider resourceProvider, CaptchaProperties captchaProperties) {
        super(resourceProvider,captchaProperties);
    }

    protected Font getFont() {
        return this.getResourceProvider().getGraphicFont();
    }

    protected int getWidth() {
        return this.getCaptchaProperties().graphics().width();
    }

    protected int getHeight() {
        return this.getCaptchaProperties().graphics().height();
    }

    protected int getLength() {
        return this.getCaptchaProperties().graphics().length();
    }

    @Override
    protected String getCacheName() {
        return CaptchaConst.CACHE_NAME_CAPTCHA_GRAPHIC;
    }

    @Override
    public AbstractCaptcha getCaptcha(String key) {
        String identity = key;
        if (StringUtils.isBlank(identity)) {
            identity = GoyaIdUtils.fastSimpleUUID();
        }

        this.put(identity);
        return getGraphicCaptcha();
    }

    @Override
    public boolean verify(Verification verification) {

        if (ObjectUtils.isEmpty(verification) || StringUtils.isEmpty(verification.getIdentity())) {
            throw new CaptchaParameterIllegalException("Parameter value is illegal");
        }

        if (StringUtils.isEmpty(verification.getCharacters())) {
            throw new CaptchaIsEmptyException("Captcha is empty");
        }

        String store = this.get(verification.getIdentity());
        if (StringUtils.isEmpty(store)) {
            throw new CaptchaHasExpiredException("Stamp is invalid!");
        }

        this.evict(verification.getIdentity());

        String real = verification.getCharacters();

        if (!Strings.CS.equals(store, real)) {
            throw new CaptchaMismatchException();
        }

        return true;
    }

    private GraphicCaptcha getGraphicCaptcha() {
        return graphicCaptcha;
    }

    protected void setGraphicCaptcha(GraphicCaptcha graphicCaptcha) {
        this.graphicCaptcha = graphicCaptcha;
    }

    @Override
    protected String nextValue(String key) {
        Metadata metadata = draw();

        GraphicCaptcha graphicCaptcha = new GraphicCaptcha();
        graphicCaptcha.setIdentity(key);
        graphicCaptcha.setGraphicImageBase64(metadata.getGraphicImageBase64());
        graphicCaptcha.setCategory(getCategory());
        this.setGraphicCaptcha(graphicCaptcha);

        return metadata.getCharacters();
    }
}
