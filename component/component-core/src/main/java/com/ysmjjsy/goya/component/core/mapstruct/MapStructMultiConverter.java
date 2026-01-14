package com.ysmjjsy.goya.component.core.mapstruct;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 00:07
 */
public interface MapStructMultiConverter<S, T1, T2> {

    T1 toTarget1(S source);

    T2 toTarget2(S source);
}
