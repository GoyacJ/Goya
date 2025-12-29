package com.ysmjjsy.goya.component.captcha.definition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>坐标</p>
 *
 * @author goya
 * @since 2025/9/30 15:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate implements Serializable {

    private int x;
    private int y;

}
