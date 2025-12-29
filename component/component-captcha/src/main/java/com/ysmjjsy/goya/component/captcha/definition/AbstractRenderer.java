package com.ysmjjsy.goya.component.captcha.definition;

import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.template.AbstractCheckTemplate;
import com.ysmjjsy.goya.component.cache.ttl.TtlStrategy;
import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.provider.ResourceProvider;
import com.ysmjjsy.goya.component.common.service.IPropertiesCacheService;
import com.ysmjjsy.goya.component.common.utils.ImgUtils;
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
    private IPropertiesCacheService iPropertiesCacheService;

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
        String image = ImgUtils.toBase64(bufferedImage, ImgUtils.IMAGE_TYPE_PNG);
        return getBase64ImagePrefix() + image;
    }

    @Override
    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        // 此处从缓存中获取配置
        CaptchaProperties properties = iPropertiesCacheService.getProperties(CaptchaProperties.class);
        return defaultSpec.toBuilder()
                .cacheLevel(properties.graphics().level())
                .ttl(properties.graphics().expire())
                .localTtlStrategy(new TtlStrategy.FixedDurationStrategy(properties.graphics().localExpire()))
                .localMaxSize(properties.graphics().localLimit())
                .build();
    }
}
