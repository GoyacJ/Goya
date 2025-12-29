package com.ysmjjsy.goya.component.captcha.renderer.behavior;

import com.ysmjjsy.goya.component.captcha.definition.Coordinate;
import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import com.ysmjjsy.goya.component.common.utils.RandomUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>文字点选信息混淆器 </p>
 *
 * @author goya
 * @since  2021/12/17 12:19
 */
@Getter
public class WordClickObfuscator {
    /**
     * 文字点选验证码文字坐标信息列表
     */
    private final List<Coordinate> coordinates;
    /**
     * 文字点选验证码校验文字
     */
    private final List<String> words;

    private String wordString;

    public WordClickObfuscator(List<String> originalWords, List<Coordinate> originalCoordinates) {
        this.coordinates = new ArrayList<>();
        this.words = new ArrayList<>();
        this.execute(originalWords, originalCoordinates);
    }

    private void execute(List<String> originalWords, List<Coordinate> originalCoordinates) {

        int[] indexes = RandomUtils.randomInts(originalWords.size());

        Arrays.stream(indexes).forEach(value -> {
            this.words.add(this.words.size(), originalWords.get(value));
            this.coordinates.add(this.coordinates.size(), originalCoordinates.get(value));
        });

        this.wordString = StringUtils.join(getWords(), ISymbolConstants.COMMA);
    }

}
