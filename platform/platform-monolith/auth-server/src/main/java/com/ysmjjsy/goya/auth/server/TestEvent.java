package com.ysmjjsy.goya.auth.server;

import com.ysmjjsy.goya.component.bus.stream.definition.AbstractBusEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/24 11:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TestEvent extends AbstractBusEvent {
    @Serial
    private static final long serialVersionUID = -48092293708781456L;

    private final String name;
}
