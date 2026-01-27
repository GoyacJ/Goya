package com.ysmjjsy.goya.component.framework.cache.support;

import com.ysmjjsy.goya.component.framework.cache.support.secret.SecretProvider;
import com.ysmjjsy.goya.component.framework.core.json.GoyaJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.GenericTypeResolver;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * <p>签章操作集合（Signed Operations）</p>
 * 基于 HMAC-SHA256 的签章
 * <p>用途：</p>
 * <ul>
 *   <li>缓存防篡改/防伪：读取时验签，失败视为未命中并可自动清理</li>
 *   <li>幂等/防重放：consumeOnce 用于一次性 token、防重提交</li>
 * </ul>
 *
 * <p><b>安全说明：</b></p>
 * <ul>
 *   <li>签名使用 HMAC-SHA256（对称密钥），密钥必须妥善保管（不要写死到仓库）。</li>
 *   <li>验签失败的默认策略建议“删除 key + 记录监控”，避免持续污染。</li>
 * </ul>
 *
 * @param <K> key 类型
 * @param <V> value 类型
 * @author goya
 * @since 2026/1/26 16:03
 */
@Slf4j
public abstract class CacheSignedSupport<K, V> extends CacheSupport<K, V> {

    private final SecretProvider secretProvider;

    protected CacheSignedSupport(String cacheName, SecretProvider secretProvider) {
        super(cacheName);
        this.secretProvider = secretProvider;
    }

    protected CacheSignedSupport(String cacheName, Duration expire, SecretProvider secretProvider) {
        super(cacheName, expire);
        this.secretProvider = secretProvider;
    }

    /**
     * 写入带签章的缓存值。
     *
     * @param key   缓存 key
     * @param value 业务值
     * @param ttl   TTL（建议必填）
     */
    public void putSigned(K key, V value, Duration ttl) {
        long ts = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString().replace("-", "");

        byte[] payload = GoyaJson.serialize(value);
        String payloadB64 = Base64.getEncoder().encodeToString(payload);

        String payloadHashB64 = Base64.getEncoder().encodeToString(sha256(payload));
        long ttlSec = (ttl == null ? -1 : ttl.toSeconds());

        String kid = secretProvider.currentKeyId();
        String canonical = canonical(cacheName, String.valueOf(key), kid, ts, nonce, ttlSec, payloadHashB64);

        String sigB64 = Base64.getEncoder().encodeToString(
                hmacSha256(secretProvider.secretFor(kid), canonical)
        );

        SignedEnvelope env = new SignedEnvelope(kid, ts, nonce, ttlSec, payloadB64, payloadHashB64, sigB64);

        cacheService.put(cacheName, key, GoyaJson.toJson(env), ttl);
    }

    /**
     * 获取并验证签章。
     *
     * <p>当验签失败时，默认应当视为未命中，并建议删除该 key（防止反复污染）。</p>
     *
     * @param key 缓存 key
     * @return 验签通过返回业务值；未命中/验签失败返回 Optional.empty()
     */
    public Optional<V> getVerified(K key) {
        String json = cacheService.get(cacheName, key, String.class);
        if (json == null) {
            return Optional.empty();
        }

        final SignedEnvelope env;
        try {
            env = GoyaJson.fromJson(json, SignedEnvelope.class);
        } catch (Exception _) {
            log.warn("Signed envelope parse failed, cacheName={}, key={}", cacheName, key);
            cacheService.delete(cacheName, key);
            return Optional.empty();
        }

        byte[] payload;
        try {
            payload = Base64.getDecoder().decode(env.payloadB64());
        } catch (Exception _) {
            log.warn("Signed payload base64 decode failed, cacheName={}, key={}", cacheName, key);
            cacheService.delete(cacheName, key);
            return Optional.empty();
        }

        String computedHashB64 = Base64.getEncoder().encodeToString(sha256(payload));
        if (constantTimeEquals(computedHashB64, env.payloadSha256B64())) {
            log.warn("Signed payload hash mismatch, cacheName={}, key={}", cacheName, key);
            cacheService.delete(cacheName, key);
            return Optional.empty();
        }

        String canonical = canonical(cacheName, String.valueOf(key), env.kid(), env.ts(), env.nonce(), env.ttlSec(), env.payloadSha256B64());
        String expectedSigB64 = Base64.getEncoder().encodeToString(
                hmacSha256(secretProvider.secretFor(env.kid()), canonical)
        );

        if (constantTimeEquals(expectedSigB64, env.sigB64())) {
            log.warn("Signed signature mismatch, cacheName={}, key={}", cacheName, key);
            cacheService.delete(cacheName, key);
            return Optional.empty();
        }

        Class<?>[] typeArguments = GenericTypeResolver.resolveTypeArguments(getClass(), CacheSignedSupport.class);
        if (typeArguments == null || typeArguments.length < 2) {
            throw new IllegalStateException("无法解析泛型类型参数: " + getClass().getName());
        }
        @SuppressWarnings("unchecked")
        Class<V> serviceClass = (Class<V>) typeArguments[1];
        V value = GoyaJson.fromJson(payload, serviceClass);
        return Optional.ofNullable(value);
    }

    /**
     * 幂等/一次性消费：原子写入占位 key。
     *
     * <p>语义：如果 key 不存在则写入并返回 true；如果已存在返回 false。</p>
     *
     * <p>典型用途：</p>
     * <ul>
     *   <li>防重提交</li>
     *   <li>回调去重</li>
     *   <li>一次性 token</li>
     * </ul>
     *
     * @param key 缓存 key
     * @param ttl TTL（强烈建议必填）
     * @return true 表示首次消费成功；false 表示已消费/已存在
     */
    public boolean consumeOnce(K key, Duration ttl) {
        // value 本身不重要，写入常量即可
        return cacheService.putIfAbsent(cacheName, key, "1", ttl);
    }


    /**
     * 构造规范化签名串。
     *
     * <p>注意：这里使用竖线分隔，并固定字段顺序，避免不同实现产生歧义。</p>
     */
    private static String canonical(String cacheName,
                                    String key,
                                    String kid,
                                    long ts,
                                    String nonce,
                                    long ttlSec,
                                    String payloadSha256B64) {
        return cacheName + "|" + key + "|" + kid + "|" + ts + "|" + nonce + "|" + ttlSec + "|" + payloadSha256B64;
    }

    private static byte[] sha256(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(bytes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] hmacSha256(byte[] secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 常量时间字符串比较，用于降低计时侧信道风险。
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return true;
        }
        byte[] ab = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        if (ab.length != bb.length) {
            return true;
        }

        int r = 0;
        for (int i = 0; i < ab.length; i++) {
            r |= (ab[i] ^ bb[i]);
        }
        return r != 0;
    }
}
