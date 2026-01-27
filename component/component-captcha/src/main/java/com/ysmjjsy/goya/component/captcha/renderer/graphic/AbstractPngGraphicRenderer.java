package com.ysmjjsy.goya.component.captcha.renderer.graphic;

import com.ysmjjsy.goya.component.captcha.definition.Metadata;
import com.ysmjjsy.goya.component.captcha.provider.ResourceProvider;
import com.ysmjjsy.goya.component.framework.common.constants.SymbolConst;
import org.apache.commons.lang3.StringUtils;

import java.awt.image.BufferedImage;
import java.time.Duration;

/**
 * <p>Png 类型图形验证码绘制器 </p>
 *
 * @author goya
 * @since 2021/12/21 23:17
 */
public abstract class AbstractPngGraphicRenderer extends AbstractBaseGraphicRenderer {

    protected AbstractPngGraphicRenderer(ResourceProvider resourceProvider, Duration expire) {
        super(resourceProvider, expire);
    }

    @Override
    public Metadata draw() {
        String[] drawCharacters = this.getDrawCharacters();

        BufferedImage bufferedImage = createPngBufferedImage(drawCharacters);

        String characters = StringUtils.join(drawCharacters, SymbolConst.BLANK);

        Metadata metadata = new Metadata();
        metadata.setGraphicImageBase64(toBase64(bufferedImage));
        metadata.setCharacters(characters);

        return metadata;
    }
}
