package com.ysmjjsy.goya.component.captcha.definition;

import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.multilevel.template.AbstractCheckTemplate;
import com.ysmjjsy.goya.component.cache.multilevel.ttl.TtlStrategy;
import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.provider.ResourceProvider;
import com.ysmjjsy.goya.component.core.utils.GoyaImgUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;

/**
 * <p>基础绘制器</p>
 *
 * @author goya
 * @since 2025/9/30 15:24
 */
public abstract class AbstractRenderer<K, V> extends AbstractCheckTemplate<K, V> implements ICaptchaRenderer {

    protected static final String BASE64_PNG_IMAGE_PREFIX = "data:image/png;base64,";
    protected static final String BASE64_GIF_IMAGE_PREFIX = "data:image/gif;base64,";

    @Autowired
    private ResourceProvider resourceProvider;

    @Autowired
    private CaptchaProperties captchaProperties;

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

    @Override
    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        // 此处从缓存中获取配置
        return defaultSpec.toBuilder()
                .cacheLevel(captchaProperties.graphics().level())
                .ttl(captchaProperties.graphics().expire())
                .localTtlStrategy(new TtlStrategy.FixedDurationStrategy(captchaProperties.graphics().localExpire()))
                .localMaxSize(captchaProperties.graphics().localLimit())
                .build();
    }
}
