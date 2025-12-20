package com.ysmjjsy.goya.component.common.code;

import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:30
 */
public class ResponseCodeRegistry {

    private final Map<String, IResponseCode> registry = new HashMap<>();

    public ResponseCodeRegistry(List<IResponseCode> codes) {
        for (IResponseCode code : codes) {
            registry.put(code.getCode(), code);
        }
    }

    @PostConstruct
    public void checkDuplicated() {
        long unique = registry.keySet().stream().distinct().count();
        if (unique != registry.size()) {
            throw new IllegalStateException("ERROR CODE DUPLICATED!");
        }
    }

    public IResponseCode get(String code) {
        return registry.get(code);
    }
}
