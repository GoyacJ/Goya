package com.ysmjjsy.goya.component.framework.bus.message;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * <p>Unified envelope for distributed/local messages.</p>
 *
 * @author goya
 * @since 2026/1/26 23:47
 */
public final class MessageEnvelope<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -694529210338638796L;

    private final String id;
    private final String type;
    private final Instant timestamp;
    private final String key;
    private final Map<String, Object> headers;
    private final T payload;

    private MessageEnvelope(Builder<T> b) {
        this.id = b.id;
        this.type = b.type;
        this.timestamp = b.timestamp;
        this.key = b.key;
        this.headers = Map.copyOf(b.headers);
        this.payload = b.payload;
    }

    public static <T> Builder<T> builder(T payload) {
        return new Builder<T>().payload(payload);
    }

    public static <T> MessageEnvelope<T> of(T payload) {
        return builder(payload).build();
    }

    public String id() { return id; }
    public String type() { return type; }
    public Instant timestamp() { return timestamp; }
    public String key() { return key; }
    public Map<String, Object> headers() { return headers; }
    public T payload() { return payload; }

    public static final class Builder<T> {
        private String id = UUID.randomUUID().toString();
        private String type;
        private Instant timestamp = Instant.now();
        private String key;
        private Map<String, Object> headers = new LinkedHashMap<>();
        private T payload;

        public Builder<T> id(String id) { this.id = Objects.requireNonNull(id); return this; }
        public Builder<T> type(String type) { this.type = type; return this; }
        public Builder<T> timestamp(Instant ts) { this.timestamp = Objects.requireNonNull(ts); return this; }
        public Builder<T> key(String key) { this.key = key; return this; }
        public Builder<T> header(String k, Object v) { this.headers.put(k, v); return this; }
        public Builder<T> headers(Map<String, Object> h) { this.headers.putAll(h); return this; }
        public Builder<T> payload(T payload) { this.payload = Objects.requireNonNull(payload); return this; }

        public MessageEnvelope<T> build() {
            if (type == null || type.isBlank()) {
                type = payload.getClass().getName();
            }
            return new MessageEnvelope<>(this);
        }
    }
}
