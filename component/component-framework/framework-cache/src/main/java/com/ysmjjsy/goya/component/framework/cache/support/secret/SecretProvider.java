package com.ysmjjsy.goya.component.framework.cache.support.secret;

/**
 * <p>签章密钥提供者</p>
 *
 * <p>用于 SignedOps 的 HMAC 签名。建议支持密钥轮换（key rotation）：</p>
 * <ul>
 *   <li>currentKeyId() 返回当前写入使用的 kid</li>
 *   <li>secretFor(kid) 返回对应 kid 的密钥</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/26 16:14
 */
public interface SecretProvider {

    /**
     * 当前写入所使用的密钥标识（Key ID）。
     *
     * @return kid
     */
    String currentKeyId();

    /**
     * 根据密钥标识获取密钥字节。
     *
     * @param keyId kid
     * @return 密钥字节
     */
    byte[] secretFor(String keyId);
}
