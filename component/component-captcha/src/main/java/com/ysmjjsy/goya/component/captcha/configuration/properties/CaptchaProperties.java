package com.ysmjjsy.goya.component.captcha.configuration.properties;

import com.ysmjjsy.goya.component.cache.multilevel.enums.CacheLevelEnum;
import com.ysmjjsy.goya.component.captcha.constants.CaptchaConst;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaCharacterEnum;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaFontEnum;
import com.ysmjjsy.goya.component.captcha.enums.FontStyleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p>验证码配置参数 </p>
 *
 * @author goya
 * @since 2021/12/11 12:25
 */
@ConfigurationProperties(prefix = CaptchaConst.PROPERTY_CAPTCHA)
public record CaptchaProperties(
        /*
         *  验证码配置参数
         */
        @DefaultValue
        Graphics graphics,
        /*
         * 水印配置
         */
        @DefaultValue
        Watermark watermark,
        /*
         * 滑块拼图验证码配置
         */
        @DefaultValue
        Jigsaw jigsaw,
        /*
         * 文字点选验证码配置
         */
        @DefaultValue
        WordClick wordClick
) {

    @Schema(description = "验证码配置参数")
    public record Graphics(
            /*
             * 验证码字符个数
             */
            @Schema(description = "验证码字符个数")
            @DefaultValue("5")
            int length,

            /*
             * 验证码显示宽度
             */
            @Schema(description = "验证码显示宽度")
            @DefaultValue("130")
            int width,

            /*
             * 验证码显示高度
             */
            @Schema(description = "验证码显示高度")
            @DefaultValue("48")
            int height,

            /*
             * 算数类型验证码算法复杂度
             */
            @Schema(description = "算数类型验证码算法复杂度")
            @DefaultValue("2")
            int complexity,

            /*
             * 字符类型
             */
            @Schema(description = "字符类型")
            @DefaultValue("NUM_AND_CHAR")
            CaptchaCharacterEnum letter,

            @Schema(description = "字体")
            @DefaultValue("LEXOGRAPHER")
            CaptchaFontEnum font,

            @DefaultValue("L1_L2")
            @Schema(description = "缓存类型")
            CacheLevelEnum level,

            @DefaultValue("PT2H")
            @Schema(description = "缓存过期时间")
            Duration expire,

            @Schema(description = "本地缓存过期时间")
            Duration localExpire,

            @Schema(description = "本地缓存数量限制")
            Integer localLimit
    ) {
    }

    /**
     * 右下角水印文字(我的水印)
     */
    @Schema(description = "右下角水印文字(我的水印)")
    public record Watermark(
            /*
             * 水印内容
             */
            @Schema(description = "水印内容")
            @DefaultValue("GOYA")
            String content,
            /*
             * 水印字体
             */
            @Schema(description = "水印字体")
            @DefaultValue("WenQuanZhengHei.ttf")
            String fontName,
            /*
             * 字体样式： 0:PLAIN, 1:BOLD, 2:ITALI；
             */
            @DefaultValue("PLAIN")
            FontStyleEnum fontStyle,

            /*
             * 水印文字中，汉字的大小，默认：25
             */
            @Schema(description = "水印文字中，汉字的大小，默认：25")
            @DefaultValue("25")
            Integer fontSize
    ) {


    }

    /**
     * 拼图滑块验证码
     */
    @Schema(description = "拼图滑块验证码")
    public record Jigsaw(
            /*
             * 拼图滑块验证码原图资源路径，格式：classpath:/xxx
             */
            @Schema(description = "拼图滑块验证码原图资源路径，格式：classpath:/xxx")
            @DefaultValue("classpath*:images/jigsaw/original/*.png")
            String originalResource,
            /*
             * 拼图滑块验证码拼图模版资源路径，格式：classpath:/xxx
             */
            @Schema(description = "拼图滑块验证码拼图模版资源路径，格式：classpath:/xxx")
            @DefaultValue("classpath*:images/jigsaw/template/*.png")
            String templateResource,

            /*
             * 滑动干扰项, 可选值为(0/1/2), 默认值为：0，即无干扰项
             */
            @Schema(description = "滑动干扰项, 可选值为(0/1/2), 默认值为：0，即无干扰项")
            @DefaultValue("0")
            Integer interference,

            /*
             * 偏差值，滑动结果与标准结果间可接受的偏差值。默认：5
             */
            @Schema(description = "偏差值，滑动结果与标准结果间可接受的偏差值。默认：5")
            @DefaultValue("5")
            Integer deviation
    ) {
    }

    /**
     * 文字点选验证码
     */
    @Schema(description = "文字点选验证码")
    public record WordClick(
            /*
              文字点选验证码资源路径，格式：classpath:/xxx
             */
            @Schema(description = "文字点选验证码资源路径，格式：classpath:/xxx")
            @DefaultValue("classpath*:images/word-click/*.png")
            String imageResource,

            /*
              文字点选验证码文字个数
             */
            @Schema(description = "文字点选验证码文字个数")
            @DefaultValue("5")
            Integer wordCount,
            /*
              随机颜色
             */
            @Schema(description = "随机颜色")
            @DefaultValue("true")
            boolean randomColor,
            /*
              字体样式： 0:PLAIN, 1:BOLD, 2:ITALI；
             */
            @Schema(description = "字体样式： 0:PLAIN, 1:BOLD, 2:ITALI")
            @DefaultValue("BOLD")
            FontStyleEnum fontStyle,
            /*
              水印字体
             */
            @Schema(description = "水印字体")
            @DefaultValue("WenQuanZhengHei.ttf")
            String fontName,
            /*
              文字点选验证码资源路径字体大小
             */
            @Schema(description = "文字点选验证码资源路径字体大小")
            @DefaultValue("25")
            Integer fontSize
    ) {
    }
}

