package com.ysmjjsy.goya.component.framework.cache.support;

/**
 * <p>签章缓存信封</p>
 *
 * <p>缓存中不直接存业务对象，而是存“payload + 元信息 + 签名”。验证通过后才反序列化为业务对象。</p>
 *
 * <p>字段说明：</p>
 * <ul>
 *   <li>kid：密钥标识，用于支持密钥轮换</li>
 *   <li>ts：写入时间（毫秒）</li>
 *   <li>nonce：随机数，增强防重放能力</li>
 *   <li>ttlSec：写入时记录的 TTL 秒数（仅用于签名输入；真实过期仍由缓存系统控制）</li>
 *   <li>payloadB64：业务对象序列化后的 Base64</li>
 *   <li>payloadSha256B64：payload 的 SHA-256 Base64（用于避免把大 payload 直接拼进签名串）</li>
 *   <li>sigB64：HMAC-SHA256 的 Base64 签名</li>
 * </ul>
 *
 * <p><b>不可变性：</b>record 天然不可变，适合做安全敏感结构（减少被误改的可能）。</p>
 *
 * @author goya
 * @since 2026/1/26 16:05
 */
public record SignedEnvelope(
        String kid,
        long ts,
        String nonce,
        long ttlSec,
        String payloadB64,
        String payloadSha256B64,
        String sigB64
) {}
