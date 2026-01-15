package com.ysmjjsy.goya.component.security.authentication.utils;

import com.ysmjjsy.goya.component.captcha.definition.Coordinate;
import com.ysmjjsy.goya.component.captcha.definition.Verification;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;
import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/4 15:28
 */
public class VerificationAssembler {

    private VerificationAssembler() {
    }

    public static Verification from(HttpServletRequest request, com.ysmjjsy.goya.security.core.enums.LoginTypeEnum grantType) {

        CaptchaCategoryEnum category = resolveCategory(request);
        Verification verification = new Verification();

        verification.setCategory(category);
        verification.setIdentity(resolveIdentity(request, grantType));
        verification.setCoordinate(resolveSingleCoordinate(request));
        verification.setCoordinates(resolveCoordinateList(request));
        verification.setCharacters(resolveCharacters(request));

        return verification;
    }

    private static CaptchaCategoryEnum resolveCategory(HttpServletRequest request) {
        String category = request.getParameter("captcha_category");
        CaptchaCategoryEnum categoryEnum = CaptchaCategoryEnum.getByCode(category);
        if (categoryEnum == null) {
            throw new IllegalArgumentException("非法验证码类型");
        }
        return categoryEnum;
    }

    private static String resolveIdentity(HttpServletRequest request, com.ysmjjsy.goya.security.core.enums.LoginTypeEnum grantType) {
        String identity = request.getParameter("identity");
        if (!StringUtils.hasText(identity)) {
            throw new IllegalArgumentException("identity 不能为空");
        }

        // 可根据 grantType 做更严格约束
        if (identity.length() > 128) {
            throw new IllegalArgumentException("identity 长度非法");
        }
        return identity;
    }

    private static Coordinate resolveSingleCoordinate(HttpServletRequest request) {
        String x = request.getParameter("captcha_coordinate_x");
        String y = request.getParameter("captcha_coordinate_y");

        if (!StringUtils.hasText(x) && !StringUtils.hasText(y)) {
            return null;
        }

        return new Coordinate(
                NumberUtils.toInt(x, 0),
                NumberUtils.toInt(y, 0)
        );
    }

    private static List<Coordinate> resolveCoordinateList(HttpServletRequest request) {
        String captchaCoordinates = request.getParameter("captcha_coordinates");
        if (!StringUtils.hasText(captchaCoordinates)) {
            return List.of();
        }

        return Arrays.stream(captchaCoordinates.split(ISymbolConstants.COMMA))
                .map(VerificationAssembler::parseCoordinate)
                .toList();
    }

    private static Coordinate parseCoordinate(String value) {
        String[] split = StringUtils.split(value, ISymbolConstants.AT);
        if (split == null || split.length < 2) {
            throw new IllegalArgumentException("非法验证码坐标格式");
        }

        return new Coordinate(
                NumberUtils.toInt(split[0], 0),
                NumberUtils.toInt(split[1], 0)
        );
    }

    private static String resolveCharacters(HttpServletRequest request) {
        return request.getParameter("captcha_characters");
    }
}
