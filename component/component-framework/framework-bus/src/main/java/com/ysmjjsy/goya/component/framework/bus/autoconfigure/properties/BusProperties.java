package com.ysmjjsy.goya.component.framework.bus.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/26 23:38
 */
@Validated
@ConfigurationProperties(prefix = PropertyConst.PROPERTY_BUS)
public record BusProperties(
        /*
          Global default binder name when multiple binders are present (e.g. kafka).
         */
        String defaultBinder,

        /*
          Prefer StreamBridge binder when it's available on classpath.
         */
        boolean preferStreamBridge,

        /*
          Bindings configuration keyed by binding name.
         */
        @DefaultValue
        Map<String, BindingProperties> bindings
) {

    public record BindingProperties(
            /*
              Destination (topic/queue/exchange...). For StreamBridge binder it typically maps to the stream binding name config.
             */
            @NotBlank
            String destination,

            /*
              Consumer group (if applicable).
             */
            String group,

            /*
              Optional binder override for this binding (e.g. rabbit).
             */
            String binder,

            /*
              Optional mapping for per-send binder override.
              If present, bus can map (binding + binder) to a dedicated stream binding name.
              Example: binderBindingNames.rabbit=orderCreated__rabbit
             */
            @DefaultValue
            Map<String, String> binderBindingNames
    ) {
    }
}
