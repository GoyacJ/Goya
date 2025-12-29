package com.ysmjjsy.goya.component.captcha.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * <p>验证数据实体</p>
 *
 * @author goya
 * @since 2025/9/30 15:23
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Verification extends AbstractCaptcha {

    @Serial
    private static final long serialVersionUID = 5181552848099569802L;
    
    /**
     * 滑块拼图验证参数
     */
    private Coordinate coordinate;
    /**
     * 文字点选验证参数
     */
    private List<Coordinate> coordinates;
    /**
     * 图形验证码验证参数
     */
    private String characters;
}
