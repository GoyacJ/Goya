package com.ysmjjsy.goya.component.captcha.renderer.behavior;


import com.ysmjjsy.goya.component.captcha.definition.AbstractRenderer;

import java.awt.*;
import java.nio.charset.StandardCharsets;

/**
 * <p>验证码通用基础类 </p>
 *
 * @param <K> 验证码缓存对应Key值的类型。
 * @param <V> 验证码缓存存储数据的值的类型
 * @author goya
 * @since 2021/12/11 15:26
 */
public abstract class AbstractBehaviorRenderer<K, V> extends AbstractRenderer<K, V> {

    protected int getEnOrZhLength(String s) {
        int enCount = 0;
        int zhCount = 0;
        for (int i = 0; i < s.length(); i++) {
            int length = String.valueOf(s.charAt(i)).getBytes(StandardCharsets.UTF_8).length;
            if (length > 1) {
                zhCount++;
            } else {
                enCount++;
            }
        }
        int zhOffset = getHalfWatermarkFontSize() * zhCount + 5;
        int enOffset = enCount * 8;
        return zhOffset + enOffset;
    }

    private int getWatermarkFontSize() {
        return getCaptchaProperties().watermark().fontSize();
    }

    private int getHalfWatermarkFontSize() {
        return getWatermarkFontSize() / 2;
    }

    protected void addWatermark(Graphics graphics, int width, int height) {
        int fontSize = getHalfWatermarkFontSize();
        Font watermakFont = this.getResourceProvider().getWaterMarkFont(fontSize);
        graphics.setFont(watermakFont);
        graphics.setColor(Color.white);
        String content = this.getCaptchaProperties().watermark().content();
        graphics.drawString(content, width - getEnOrZhLength(content), height - getHalfWatermarkFontSize() + 7);
    }

    protected boolean isUnderOffset(int actualValue, int standardValue, int threshold) {
        return actualValue < standardValue - threshold;
    }

    protected boolean isOverOffset(int actualValue, int standardValue, int threshold) {
        return actualValue > standardValue + threshold;
    }

    protected boolean isDeflected(int actualValue, int standardValue, int threshold) {
        return isUnderOffset(actualValue, standardValue, threshold) || isOverOffset(actualValue, standardValue, threshold);
    }
}
