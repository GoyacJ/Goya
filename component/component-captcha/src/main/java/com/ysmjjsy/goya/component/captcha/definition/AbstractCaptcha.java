package com.ysmjjsy.goya.component.captcha.definition;

import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;
import com.ysmjjsy.goya.component.framework.common.pojo.DTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

/**
 * <p>验证码返回数据基础类</p>
 *
 * @author goya
 * @since 2025/9/30 15:21
 */
@Data
public abstract class AbstractCaptcha implements DTO {

    @Serial
    private static final long serialVersionUID = -5908240064749675207L;

    @Schema(description = "验证码身份")
    private String identity;

    @Schema(description = "验证码类别")
    private CaptchaCategoryEnum category;
}
