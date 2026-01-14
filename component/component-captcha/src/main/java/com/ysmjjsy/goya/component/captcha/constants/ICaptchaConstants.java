package com.ysmjjsy.goya.component.captcha.constants;

import com.ysmjjsy.goya.component.cache.constants.CacheConst;
import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/9/30 16:16
 */
public interface ICaptchaConstants {

    String PROPERTY_CAPTCHA = IBaseConstants.PROPERTY_PLATFORM + ".captcha";

    String CACHE_NAME_CAPTCHA = CacheConst.CACHE_PREFIX + "captcha:";

    String CACHE_NAME_CAPTCHA_JIGSAW = CACHE_NAME_CAPTCHA + "jigsaw:";
    String CACHE_NAME_CAPTCHA_WORD_CLICK = CACHE_NAME_CAPTCHA + "word_click:";
    String CACHE_NAME_CAPTCHA_GRAPHIC = CACHE_NAME_CAPTCHA + "graphic:";
}
