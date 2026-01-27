package com.ysmjjsy.goya.component.captcha.renderer.graphic;

import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.definition.Metadata;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;
import com.ysmjjsy.goya.component.captcha.provider.RandomProvider;
import com.ysmjjsy.goya.component.captcha.provider.ResourceProvider;
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.image.BufferedImage;

/**
 * <p>算数类型一般验证码 </p>
 *
 * @author goya
 * @since 2021/12/20 22:55
 */
@Slf4j
public class ArithmeticCaptchaRenderer extends AbstractBaseGraphicRenderer {

    private final int complexity;
    /**
     * 计算结果
     */
    private String computedResult;

    public ArithmeticCaptchaRenderer(ResourceProvider resourceProvider, CaptchaProperties captchaProperties) {
        super(resourceProvider,captchaProperties.graphics().expire());
        this.complexity = this.getCaptchaProperties().graphics().complexity();
    }

    @Override
    public CaptchaCategoryEnum getCategory() {
        return CaptchaCategoryEnum.ARITHMETIC;
    }

    @Override
    protected String[] getDrawCharacters() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < complexity; i++) {
            builder.append(RandomProvider.randomInt(10));
            if (i < complexity - 1) {
                int type = RandomProvider.randomInt(1, 4);
                if (type == 1) {
                    builder.append("+");
                } else if (type == 2) {
                    builder.append("-");
                } else if (type == 3) {
                    builder.append("x");
                }
            }
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");

        try {
            computedResult = String.valueOf(engine.eval(builder.toString().replaceAll("x", "*")));
        } catch (ScriptException e) {
            log.error("[Goya] |- Arithmetic png captcha eval expression error！", e);
        }

        builder.append("=?");

        String result = builder.toString();
        return result.split("(?!^)");
    }

    @Override
    public Metadata draw() {
        String[] drawContent = getDrawCharacters();
        BufferedImage bufferedImage = createArithmeticBufferedImage(drawContent);

        Metadata metadata = new Metadata();
        metadata.setGraphicImageBase64(toBase64(bufferedImage));
        metadata.setCharacters(this.computedResult);
        return metadata;
    }
}
