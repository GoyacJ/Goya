package com.ysmjjsy.goya.component.captcha.definition;

import lombok.Data;

import java.util.List;

/**
 * <p>图形验证码元数据</p>
 *
 * @author goya
 * @since 2025/9/30 15:19
 */
@Data
public class Metadata {

    /**
     * 滑块拼图验证码生成的带抠图背景图Base64
     */
    private String originalImageBase64;
    /**
     * 滑块拼图验证码滑块拼图Base64
     */
    private String sliderImageBase64;
    /**
     * 滑块拼图验证码抠图位置坐标。
     */
    private Coordinate coordinate;
    /**
     * 文字点选验证码生成的带文字背景图。
     */
    private String wordClickImageBase64;
    /**
     * 文字点选验证码文字坐标信息列表
     */
    private List<Coordinate> coordinates;
    /**
     * 文字点选验证码校验文字
     */
    private List<String> words;
    /**
     * 图形验证码生成的图片
     */
    private String graphicImageBase64;
    /**
     * 图形验证码校验内容
     */
    private String characters;
}
