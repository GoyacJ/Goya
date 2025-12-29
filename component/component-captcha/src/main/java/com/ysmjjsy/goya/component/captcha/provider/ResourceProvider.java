package com.ysmjjsy.goya.component.captcha.provider;

import com.google.common.collect.Maps;
import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaResourceEnum;
import com.ysmjjsy.goya.component.captcha.enums.FontStyleEnum;
import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import com.ysmjjsy.goya.component.common.utils.*;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>验证码静态资源加载器 </p>
 *
 * @author goya
 * @since 2021/12/22 18:52
 */
@Component
public class ResourceProvider implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ResourceProvider.class);

    private static final String FONT_RESOURCE = "classpath*:/fonts/*.ttf";
    private static final String FONT_FOLDER = "/usr/share/fonts/goya/";

    private final Map<String, String[]> imageIndexes = Maps.newConcurrentMap();
    private final Map<String, String> jigsawOriginalImages = Maps.newConcurrentMap();
    private final Map<String, String> jigsawTemplateImages = Maps.newConcurrentMap();
    private final Map<String, String> wordClickImages = Maps.newConcurrentMap();
    @Getter
    private final CaptchaProperties captchaProperties;
    private Map<String, Font> fonts = Maps.newConcurrentMap();

    public ResourceProvider(CaptchaProperties captchaProperties) {
        this.captchaProperties = captchaProperties;
    }

    private static Map<String, String> getImages(String location) {
        if (ResourceResolverUtils.isClasspathAllUrl(location)) {
            try {
                Resource[] resources = ResourceResolverUtils.getResources(location);
                Map<String, String> images = Maps.newConcurrentMap();
                if (ArrayUtils.isNotEmpty(resources)) {
                    Arrays.stream(resources).forEach(resource -> {
                        String data = ResourceResolverUtils.toBase64(resource);
                        if (StringUtils.isNotBlank(data)) {
                            images.put(IdentityUtils.fastSimpleUUID(), data);
                        }
                    });
                }
                return images;
            } catch (IOException e) {
                log.error("[GOYA] |- Analysis the  location [{}] catch io error!", location, e);
            }
        }

        return new ConcurrentHashMap<>(8);
    }

    private static Font getFont(Resource resource) {

        try {
            return FontUtils.loadFont(resource.getInputStream());
        } catch (CommonException e) {
            // 虽然 java.awt.Font 抛出的是 IOException, 因为使用 Hutool FontUtil 将错误又包装了一次。所以出错时必须要拦截 IORuntimeException，否则会导致错误不被拦截直接抛出，应用启动失败。
            log.warn("[GOYA] |- Can not read font in the resources folder, maybe in docker.");
            // TODO: 2022-10-21 尝试在 docker alpine 下解决字体问题的多种方式之一。目前改用 debian，下面代码已经不再需要。暂留，确保确实没有问题后再做处理
            Font fontInfileSystem = getFontUnderDocker(resource.getFilename());
            if (ObjectUtils.isNotEmpty(fontInfileSystem)) {
                return fontInfileSystem;
            }
        } catch (IOException e) {
            log.error("[GOYA] |- Resource object in resources folder catch io error!", e);
        }

        return null;
    }

    private static Font getFontUnderDocker(String filename) {
        if (ManagementUtils.isLinux()) {
            String path = FONT_FOLDER + filename;

            File file = new File(path);
            if (ObjectUtils.isNotEmpty(file) && FileUtils.exists(file)) {
                try {
                    Font font = FontUtils.loadFont(file);
                    log.debug("[GOYA] |- Read font [{}] under the DOCKER.", font.getFontName());
                    return font;
                } catch (CommonException e) {
                    log.error("[GOYA] |- Read font under the DOCKER catch error.");
                } catch (NullPointerException e) {
                    log.error("[GOYA] |- Read font under the DOCKER catch null error.");
                }
            }
        }
        return null;
    }

    private static Map<String, Font> getFonts(String location) {

        if (ResourceResolverUtils.isClasspathAllUrl(location)) {
            try {
                Resource[] resources = ResourceResolverUtils.getResources(location);
                Map<String, Font> fonts = new ConcurrentHashMap<>();
                if (ArrayUtils.isNotEmpty(resources)) {
                    Arrays.stream(resources).forEach(resource -> {
                        Font font = getFont(resource);
                        if (ObjectUtils.isNotEmpty(font)) {
                            fonts.put(resource.getFilename(), font);
                        }
                    });
                }
                return fonts;
            } catch (IOException e) {
                log.error("[GOYA] |- Analysis the  location [{}] catch io error!", location, e);
            }
        }

        return new ConcurrentHashMap<>(8);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        String systemName = ManagementUtils.getOsName();
        log.debug("[GOYA] |- Before captcha resource loading, check system. Current system is [{}]", systemName);

        log.debug("[GOYA] |- Captcha resource loading is BEGIN！");

        loadImages(jigsawOriginalImages, getCaptchaProperties().jigsaw().originalResource(), CaptchaResourceEnum.JIGSAW_ORIGINAL);

        loadImages(jigsawTemplateImages, getCaptchaProperties().jigsaw().templateResource(), CaptchaResourceEnum.JIGSAW_TEMPLATE);

        loadImages(wordClickImages, getCaptchaProperties().wordClick().imageResource(), CaptchaResourceEnum.WORD_CLICK);

        loadFonts();

        log.debug("[GOYA] |- Jigsaw captcha resource loading is END！");
    }

    private void loadImages(Map<String, String> container, String location, CaptchaResourceEnum captchaResource) {
        Map<String, String> resource = getImages(location);

        if (MapUtils.isNotEmpty(resource)) {
            container.putAll(resource);
            log.debug("[GOYA] |- {} load complete, total number is [{}]", captchaResource.getCode(), resource.size());
            imageIndexes.put(captchaResource.name(), resource.keySet().toArray(new String[0]));
        }
    }

    private void loadFonts() {
        if (MapUtils.isEmpty(fonts)) {
            this.fonts = getFonts(FONT_RESOURCE);
            log.debug("[GOYA] |- Font load complete, total number is [{}]", fonts.size());
        }
    }

    private Font getDefaultFont(String fontName, int fontSize, FontStyleEnum fontStyle) {
        if (StringUtils.isNotBlank(fontName)) {
            return new Font(fontName, fontStyle.getMapping(), fontSize);
        } else {
            return new Font("WenQuanYi Zen Hei", fontStyle.getMapping(), fontSize);
        }
    }

    public Font getFont(String fontName, int fontSize, FontStyleEnum fontStyle) {
        if (MapUtils.isEmpty(fonts) || ObjectUtils.isEmpty(fonts.get(fontName))) {
            return getDefaultFont(fontName, fontSize, fontStyle);
        } else {
            return fonts.get(fontName).deriveFont(fontStyle.getMapping(), Integer.valueOf(fontSize).floatValue());
        }
    }

    public Font getFont(String fontName) {
        return getFont(fontName, 32, FontStyleEnum.BOLD);
    }

    public Font getGraphicFont() {
        String fontName = getCaptchaProperties().graphics().font().getFontName();
        return this.getFont(fontName);
    }

    public Font getWaterMarkFont(int fontSize) {
        String fontName = getCaptchaProperties().watermark().fontName();
        FontStyleEnum fontStyle = getCaptchaProperties().watermark().fontStyle();
        return getFont(fontName, fontSize, fontStyle);
    }

    public Font getChineseFont() {
        return getFont("WenQuanYi Zen Hei", 25, FontStyleEnum.PLAIN);
    }

    private String getRandomBase64Image(Map<String, String> container, CaptchaResourceEnum captchaResource) {
        String[] data = this.imageIndexes.get(captchaResource.name());
        if (ArrayUtils.isNotEmpty(data)) {
            int randomInt = RandomProvider.randomInt(0, data.length);
            return container.get(data[randomInt]);
        }
        return null;
    }

    protected BufferedImage getRandomImage(Map<String, String> container, CaptchaResourceEnum captchaResource) {
        String data = getRandomBase64Image(container, captchaResource);
        if (StringUtils.isNotBlank(data)) {
            return ImgUtils.toImage(data);
        }

        return null;
    }

    public String getRandomBase64OriginalImage() {
        return getRandomBase64Image(jigsawOriginalImages, CaptchaResourceEnum.JIGSAW_ORIGINAL);
    }

    public String getRandomBase64TemplateImage() {
        return getRandomBase64Image(jigsawTemplateImages, CaptchaResourceEnum.JIGSAW_TEMPLATE);
    }

    public BufferedImage getRandomOriginalImage() {
        return getRandomImage(jigsawOriginalImages, CaptchaResourceEnum.JIGSAW_ORIGINAL);
    }

    public BufferedImage getRandomTemplateImage() {
        return getRandomImage(jigsawOriginalImages, CaptchaResourceEnum.JIGSAW_ORIGINAL);
    }

    public BufferedImage getRandomWordClickImage() {
        return getRandomImage(wordClickImages, CaptchaResourceEnum.WORD_CLICK);
    }
}
