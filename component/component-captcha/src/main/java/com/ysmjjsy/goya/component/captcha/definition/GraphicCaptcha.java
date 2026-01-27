package com.ysmjjsy.goya.component.captcha.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>图形验证码</p>
 *
 * @author goya
 * @since 2025/9/30 15:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GraphicCaptcha extends AbstractCaptcha {

    @Serial
    private static final long serialVersionUID = 5037611256028933366L;

    /**
     * 图形验证码成的图。
     */
    private String graphicImageBase64;
}
