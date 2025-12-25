package com.ysmjjsy.goya.component.bus.constants;

import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/25 15:41
 */
public interface IBusConstants {

    String PROPERTY_BUS = IBaseConstants.PROPERTY_PLATFORM + ".bus";

    String MARK_LOCAL = "LOCAL";
    String MARK_REMOTE = "REMOTE";
    String MARK_KAFKA = MARK_REMOTE + "_KAFKA";
}
