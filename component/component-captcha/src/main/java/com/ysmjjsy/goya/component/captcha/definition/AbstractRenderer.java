package com.ysmjjsy.goya.component.captcha.definition;

import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.provider.ResourceProvider;
import com.ysmjjsy.goya.component.framework.cache.support.CacheSupport;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaImgUtils;

import java.awt.image.BufferedImage;
import java.time.Duration;

/**
 * <p>基础绘制器</p>
 *
 * @author goya
 * @since 2025/9/30 15:24
 */
public abstract class AbstractRenderer<K, V> extends CacheSupport<K, V> implements ICaptchaRenderer {

    protected static final String BASE64_PNG_IMAGE_PREFIX = "data:image/png;base64,";
    protected static final String BASE64_GIF_IMAGE_PREFIX = "data:image/gif;base64,";

    private final ResourceProvider resourceProvider;

    protected AbstractRenderer(ResourceProvider resourceProvider, String cacheName, Duration expire) {
        super(cacheName, expire);
        this.resourceProvider = resourceProvider;
    }

    protected ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    protected CaptchaProperties getCaptchaProperties() {
        return getResourceProvider().getCaptchaProperties();
    }

    protected String getBase64ImagePrefix() {
        return BASE64_PNG_IMAGE_PREFIX;
    }

    protected String toBase64(BufferedImage bufferedImage) {
        String image = GoyaImgUtils.toBase64(bufferedImage, GoyaImgUtils.IMAGE_TYPE_PNG);
        return getBase64ImagePrefix() + image;
    }
}
