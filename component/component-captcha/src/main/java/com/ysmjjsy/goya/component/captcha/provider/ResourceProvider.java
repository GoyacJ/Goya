package com.ysmjjsy.goya.component.captcha.provider;

import com.google.common.collect.Maps;
import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaResourceEnum;
import com.ysmjjsy.goya.component.captcha.enums.FontStyleEnum;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.utils.*;
import com.ysmjjsy.goya.component.framework.core.context.SpringContext;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

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

    @Autowired
    public ResourceProvider(CaptchaProperties captchaProperties) {
        this.captchaProperties = captchaProperties;
    }

    private static Map<String, String> getImages(String location) {
        if (SpringContext.isClasspathAllUrl(location)) {
            try {
                Resource[] resources = SpringContext.getResources(location);
                Map<String, String> images = Maps.newConcurrentMap();
                if (ArrayUtils.isNotEmpty(resources)) {
                    Arrays.stream(resources).forEach(resource -> {
                        String data = SpringContext.toBase64(resource);
                        if (StringUtils.isNotBlank(data)) {
                            images.put(GoyaIdUtils.fastSimpleUUID(), data);
                        }
                    });
                }
                return images;
            } catch (Exception e) {
                log.error("[Goya] |- Analysis the  location [{}] catch io error!", location, e);
            }
        }

        return new ConcurrentHashMap<>(8);
    }

    private static Font getFont(Resource resource) {

        try {
            return GoyaFontUtils.loadFont(resource.getInputStream());
        } catch (GoyaException _) {
            log.warn("[Goya] |- Can not read font in the resources folder, maybe in docker.");
            Font fontInfileSystem = getFontUnderDocker(resource.getFilename());
            if (ObjectUtils.isNotEmpty(fontInfileSystem)) {
                return fontInfileSystem;
            }
        } catch (IOException e) {
            log.error("[Goya] |- Resource object in resources folder catch io error!", e);
        }

        return null;
    }

    private static Font getFontUnderDocker(String filename) {
        if (GoyaManagementUtils.isLinux()) {
            String path = FONT_FOLDER + filename;

            File file = new File(path);
            if (ObjectUtils.isNotEmpty(file) && GoyaFileUtils.exists(file)) {
                try {
                    Font font = GoyaFontUtils.loadFont(file);
                    log.debug("[Goya] |- Read font [{}] under the DOCKER.", font.getFontName());
                    return font;
                } catch (GoyaException _) {
                    log.error("[Goya] |- Read font under the DOCKER catch error.");
                } catch (NullPointerException _) {
                    log.error("[Goya] |- Read font under the DOCKER catch null error.");
                }
            }
        }
        return null;
    }

    private static Map<String, Font> getFonts(String location) {

        if (SpringContext.isClasspathAllUrl(location)) {
            try {
                Resource[] resources = SpringContext.getResources(location);
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
            } catch (Exception e) {
                log.error("[Goya] |- Analysis the  location [{}] catch io error!", location, e);
            }
        }

        return new ConcurrentHashMap<>(8);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        String systemName = GoyaManagementUtils.getOsName();
        log.debug("[Goya] |- Before captcha resource loading, check system. Current system is [{}]", systemName);

        log.debug("[Goya] |- Captcha resource loading is BEGIN！");

        loadImages(jigsawOriginalImages, getCaptchaProperties().jigsaw().originalResource(), CaptchaResourceEnum.JIGSAW_ORIGINAL);

        loadImages(jigsawTemplateImages, getCaptchaProperties().jigsaw().templateResource(), CaptchaResourceEnum.JIGSAW_TEMPLATE);

        loadImages(wordClickImages, getCaptchaProperties().wordClick().imageResource(), CaptchaResourceEnum.WORD_CLICK);

        loadFonts();

        log.debug("[Goya] |- Jigsaw captcha resource loading is END！");
    }

    private void loadImages(Map<String, String> container, String location, CaptchaResourceEnum captchaResource) {
        Map<String, String> resource = getImages(location);

        if (MapUtils.isNotEmpty(resource)) {
            container.putAll(resource);
            log.debug("[Goya] |- {} load complete, total number is [{}]", captchaResource.getCode(), resource.size());
            imageIndexes.put(captchaResource.name(), resource.keySet().toArray(new String[0]));
        }
    }

    private void loadFonts() {
        if (MapUtils.isEmpty(fonts)) {
            this.fonts = getFonts(FONT_RESOURCE);
            log.debug("[Goya] |- Font load complete, total number is [{}]", fonts.size());
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
            return GoyaImgUtils.toImage(data);
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
