package com.ysmjjsy.goya.component.captcha.renderer.behavior;

import com.ysmjjsy.goya.component.captcha.definition.AbstractCaptcha;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>滑块拼图验证码返回前端信息 </p>
 *
 * @author goya
 * @since 2021/12/13 17:06
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JigsawCaptcha extends AbstractCaptcha {

    @Serial
    private static final long serialVersionUID = 7839067141154290299L;

    private String originalImageBase64;

    private String sliderImageBase64;
}
