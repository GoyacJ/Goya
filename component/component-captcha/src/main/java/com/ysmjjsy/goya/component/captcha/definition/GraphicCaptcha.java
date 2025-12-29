package com.ysmjjsy.goya.component.captcha.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>图形验证码</p>
 *
 * @author goya
 * @since 2025/9/30 15:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GraphicCaptcha extends AbstractCaptcha {

    /**
     * 图形验证码成的图。
     */
    private String graphicImageBase64;
}
