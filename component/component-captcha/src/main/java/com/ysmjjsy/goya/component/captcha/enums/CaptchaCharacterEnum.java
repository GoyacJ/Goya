package com.ysmjjsy.goya.component.captcha.enums;

import com.ysmjjsy.goya.component.captcha.provider.RandomProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * <p>验证码字符类型 </p>
 *
 * @author goya
 * @since 2021/12/21 16:26
 */
@Getter
@AllArgsConstructor
public enum CaptchaCharacterEnum implements Serializable {

    /**
     * 验证码字母显示类别
     */
    NUM_AND_CHAR(RandomProvider.NUM_MIN_INDEX, RandomProvider.CHAR_MAX_INDEX, "数字和字母混合"),
    ONLY_NUM(RandomProvider.NUM_MIN_INDEX, RandomProvider.NUM_MAX_INDEX, "纯数字"),
    ONLY_CHAR(RandomProvider.CHAR_MIN_INDEX, RandomProvider.CHAR_MAX_INDEX, "纯字母"),
    ONLY_UPPER_CHAR(RandomProvider.UPPER_MIN_INDEX, RandomProvider.UPPER_MAX_INDEX, "纯大写字母"),
    ONLY_LOWER_CHAR(RandomProvider.LOWER_MIN_INDEX, RandomProvider.LOWER_MAX_INDEX, "纯小写字母"),
    NUM_AND_UPPER_CHAR(RandomProvider.NUM_MIN_INDEX, RandomProvider.UPPER_MAX_INDEX, "数字和大写字母");

    /**
     * 字符枚举值开始位置
     */
    private final int start;
    /**
     * 字符枚举值结束位置
     */
    private final int end;
    /**
     * 类型说明
     */
    private final String description;

}
