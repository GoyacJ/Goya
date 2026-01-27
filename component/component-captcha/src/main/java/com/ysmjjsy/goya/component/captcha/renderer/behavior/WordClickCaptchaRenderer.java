package com.ysmjjsy.goya.component.captcha.renderer.behavior;

import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.constants.CaptchaConst;
import com.ysmjjsy.goya.component.captcha.definition.AbstractCaptcha;
import com.ysmjjsy.goya.component.captcha.definition.Coordinate;
import com.ysmjjsy.goya.component.captcha.definition.Metadata;
import com.ysmjjsy.goya.component.captcha.definition.Verification;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;
import com.ysmjjsy.goya.component.captcha.enums.FontStyleEnum;
import com.ysmjjsy.goya.component.captcha.exception.CaptchaException;
import com.ysmjjsy.goya.component.captcha.provider.RandomProvider;
import com.ysmjjsy.goya.component.captcha.provider.ResourceProvider;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaIdUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>文字点选验证码处理器 </p>
 *
 * @author goya
 * @since 2021/12/14 9:31
 */
public class WordClickCaptchaRenderer extends AbstractBehaviorRenderer<String, List<Coordinate>> {

    public WordClickCaptchaRenderer(ResourceProvider resourceProvider, CaptchaProperties captchaProperties) {
        super(resourceProvider, CaptchaConst.CACHE_NAME_CAPTCHA_WORD_CLICK, captchaProperties.graphics().expire());
    }

    private WordClickCaptcha wordClickCaptcha;

    private Font getFont() {
        int fontSize = this.getCaptchaProperties().wordClick().fontSize();
        String fontName = this.getCaptchaProperties().watermark().fontName();
        FontStyleEnum fontStyle = this.getCaptchaProperties().watermark().fontStyle();
        return this.getResourceProvider().getFont(fontName, fontSize, fontStyle);
    }

    @Override
    public CaptchaCategoryEnum getCategory() {
        return CaptchaCategoryEnum.WORD_CLICK;
    }

    public List<Coordinate> generateValue(String key) {

        Metadata metadata = draw();

        WordClickObfuscator wordClickObfuscator = new WordClickObfuscator(metadata.getWords(), metadata.getCoordinates());

        WordClickCaptcha wordClickCaptcha = new WordClickCaptcha();
        wordClickCaptcha.setIdentity(key);
        wordClickCaptcha.setWordClickImageBase64(metadata.getWordClickImageBase64());
        wordClickCaptcha.setWords(wordClickObfuscator.getWordString());
        wordClickCaptcha.setWordsCount(metadata.getWords().size());
        this.wordClickCaptcha = wordClickCaptcha;
        return wordClickObfuscator.getCoordinates();
    }

    @Override
    public AbstractCaptcha getCaptcha(String key) {
        String identity = key;
        if (StringUtils.isBlank(identity)) {
            identity = GoyaIdUtils.fastUUID();
        }

        this.put(identity, generateValue(identity));
        return this.wordClickCaptcha;
    }

    @Override
    public boolean verify(Verification verification) {

        if (ObjectUtils.isEmpty(verification) || CollectionUtils.isEmpty(verification.getCoordinates())) {
            throw new CaptchaException("Parameter Stamp value is null");
        }

        List<Coordinate> store = this.get(verification.getIdentity());
        if (CollectionUtils.isEmpty(store)) {
            throw new CaptchaException("Stamp is invalid!");
        }

        this.delete(verification.getIdentity());

        List<Coordinate> real = verification.getCoordinates();

        for (int i = 0; i < store.size(); i++) {
            if (isDeflected(real.get(i).getX(), store.get(i).getX(), this.getFontSize()) || isDeflected(real.get(i).getX(), store.get(i).getX(), this.getFontSize())) {
                throw new CaptchaException("Stamp is invalid!");
            }
        }

        return true;
    }

    @Override
    public Metadata draw() {

        BufferedImage backgroundImage = this.getResourceProvider().getRandomWordClickImage();

        int wordCount = getCaptchaProperties().wordClick().wordCount();

        List<String> words = RandomProvider.randomWords(wordCount);

        Graphics backgroundGraphics = backgroundImage.getGraphics();
        int backgroundImageWidth = backgroundImage.getWidth();
        int backgroundImageHeight = backgroundImage.getHeight();

        List<Coordinate> coordinates = IntStream.range(0, words.size())
                .mapToObj(index -> drawWord(backgroundGraphics, backgroundImageWidth, backgroundImageHeight, index, wordCount, words.get(index))).collect(Collectors.toList());

        addWatermark(backgroundGraphics, backgroundImageWidth, backgroundImageHeight);

        //创建合并图片
        BufferedImage combinedImage = new BufferedImage(backgroundImageWidth, backgroundImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics combinedGraphics = combinedImage.getGraphics();
        combinedGraphics.drawImage(backgroundImage, 0, 0, null);

        //定义随机1到arr.length某一个字不参与校验
        int excludeWordIndex = RandomProvider.randomInt(1, wordCount) - 1;
        words.remove(excludeWordIndex);
        coordinates.remove(excludeWordIndex);

        Metadata metadata = new Metadata();
        metadata.setWordClickImageBase64(toBase64(backgroundImage));
        metadata.setCoordinates(coordinates);
        metadata.setWords(words);
        return metadata;
    }

    private Coordinate drawWord(Graphics graphics, int width, int height, int index, int wordCount, String word) {
        Coordinate coordinate = randomWordCoordinate(width, height, index, wordCount);

        //随机字体颜色
        if (getCaptchaProperties().wordClick().randomColor()) {
            graphics.setColor(new Color(RandomProvider.randomInt(1, 255), RandomProvider.randomInt(1, 255), RandomProvider.randomInt(1, 255)));
        } else {
            graphics.setColor(Color.BLACK);
        }

        // 设置角度
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.toRadians(RandomProvider.randomInt(-45, 45)), 0, 0);
        Font rotatedFont = this.getFont().deriveFont(affineTransform);
        graphics.setFont(rotatedFont);
        graphics.drawString(word, coordinate.getX(), coordinate.getY());
        return coordinate;
    }

    private int getFontSize() {
        return this.getCaptchaProperties().wordClick().fontSize();
    }

    private int getHalfFontSize() {
        return this.getFontSize() / 2;
    }

    /**
     * 根据汉字排序的枚举值值，计算汉字的坐标点。
     *
     * @param backgroundImageWidth  图片宽度
     * @param backgroundImageHeight 图片高度
     * @param wordIndex             汉字排序的枚举值值
     * @param wordCount             显示汉字的总数量
     * @return 当前汉字的坐标 {@link  Coordinate}
     */
    private Coordinate randomWordCoordinate(int backgroundImageWidth, int backgroundImageHeight, int wordIndex, int wordCount) {
        int wordSize = getFontSize();
        int halfWordSize = getHalfFontSize();

        int averageWidth = backgroundImageWidth / (wordCount + 1);
        int x, y;
        if (averageWidth < halfWordSize) {
            x = RandomProvider.randomInt(getStartInclusive(halfWordSize), backgroundImageWidth);
        } else {
            if (wordIndex == 0) {
                x = RandomProvider.randomInt(getStartInclusive(halfWordSize), getEndExclusive(wordIndex, averageWidth, halfWordSize));
            } else {
                x = RandomProvider.randomInt(averageWidth * wordIndex + halfWordSize, getEndExclusive(wordIndex, averageWidth, halfWordSize));
            }
        }
        y = RandomProvider.randomInt(wordSize, backgroundImageHeight - wordSize);
        return new Coordinate(x, y);
    }

    /**
     * 获取默认随机数起始点
     *
     * @param halfWordSize 半个汉字的大小
     * @return 最小的随机 x 坐标
     */
    private int getStartInclusive(int halfWordSize) {
        return 1 + halfWordSize;
    }

    /**
     * 获取默认随机数终点
     *
     * @param wordIndex    汉字的枚举值值(当前是第几个汉字)
     * @param averageWidth 栅格宽度
     * @param halfWordSize 半个汉字的大小
     * @return 最大的随机 x 坐标
     */
    private int getEndExclusive(int wordIndex, int averageWidth, int halfWordSize) {
        return averageWidth * (wordIndex + 1) - halfWordSize;
    }

}
