package com.ysmjjsy.goya.component.framework.cache.support.secret;

import lombok.RequiredArgsConstructor;

/**
 * <p>单密钥实现（不支持轮换）</p>
 *
 * <p>适合早期落地；后续可替换为多密钥 + 轮换策略（例如从 KMS/配置中心加载）。</p>
 *
 * @author goya
 * @since 2026/1/26 16:15
 */
@RequiredArgsConstructor
public class SingleSecretProvider implements SecretProvider {

    private final String keyId;
    private final byte[] secret;

    @Override
    public String currentKeyId() {
        return keyId;
    }

    @Override
    public byte[] secretFor(String keyId) {
        if (!this.keyId.equals(keyId)) {
            throw new IllegalArgumentException("Unknown kid: " + keyId);
        }
        return secret;
    }
}
