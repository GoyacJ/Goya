package com.ysmjjsy.goya.component.captcha.definition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = 2251578238519558767L;
    
    private int x;
    private int y;

}
