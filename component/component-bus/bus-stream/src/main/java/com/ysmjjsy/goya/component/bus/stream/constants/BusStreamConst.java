package com.ysmjjsy.goya.component.bus.stream.constants;

import com.ysmjjsy.goya.component.bus.core.constants.BusConst;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/25 15:41
 */
public interface BusStreamConst {

    String PROPERTY_BUS_STREAM = BusConst.PROPERTY_BUS + ".stream";


    String MARK_LOCAL = "LOCAL";
    String MARK_REMOTE = "REMOTE";
    String MARK_KAFKA = MARK_REMOTE + "_KAFKA";
}
