package com.ysmjjsy.goya.component.common.mapstruct;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 00:07
 */
public interface IMultiConverter <S, T1, T2> {

    T1 toTarget1(S source);

    T2 toTarget2(S source);
}
