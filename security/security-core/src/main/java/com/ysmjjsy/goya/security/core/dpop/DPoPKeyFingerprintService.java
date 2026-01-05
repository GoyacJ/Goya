package com.ysmjjsy.goya.security.core.dpop;

import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import com.ysmjjsy.goya.security.core.exception.SecurityAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * <p>DPoP公钥指纹服务</p>
 * <p>从DPoP Proof JWT中提取公钥指纹（jkt - JSON Web Key Thumbprint）</p>
 * <p>实现RFC 9449规范的DPoP支持</p>
 *
 * <p>参考文档：</p>
 * <ul>
 *   <li><a href="https://www.rfc-editor.org/rfc/rfc9449">RFC 9449 - OAuth 2.0 Demonstrating Proof-of-Possession at the Application Layer (DPoP)</a></li>
 *   <li><a href="https://www.rfc-editor.org/rfc/rfc7638">RFC 7638 - JSON Web Key (JWK) Thumbprint</a></li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
public class DPoPKeyFingerprintService {

    /**
     * 从DPoP Proof JWT中提取公钥指纹
     * <p>根据RFC 7638规范，使用SHA-256算法计算JWK的指纹</p>
     *
     * @param dPoPProofJwt DPoP Proof JWT
     * @return 公钥指纹（Base64URL编码）
     */
    public String extractFingerprint(Jwt dPoPProofJwt) {
        if (dPoPProofJwt == null) {
            return null;
        }

        try {
            // 1. 从JWT Header中提取JWK（jwk字段）
            Object jwkObject = dPoPProofJwt.getHeaders().get("jwk");
            if (jwkObject == null) {
                log.warn("[Goya] |- security [core] DPoP Proof JWT does not contain 'jwk' header");
                return null;
            }

            // 2. 将JWK转换为规范化的JSON字符串（RFC 7638要求）
            String normalizedJwk = normalizeJwk(jwkObject);

            // 3. 计算SHA-256哈希
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizedJwk.getBytes(StandardCharsets.UTF_8));

            // 4. Base64URL编码（RFC 7638要求）
            String thumbprint = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            log.debug("[Goya] |- security [core] DPoP key fingerprint extracted: {}", thumbprint);
            return thumbprint;
        } catch (NoSuchAlgorithmException e) {
            log.error("[Goya] |- security [core] SHA-256 algorithm not available", e);
            return null;
        } catch (Exception e) {
            log.error("[Goya] |- security [core] Failed to extract DPoP key fingerprint", e);
            return null;
        }
    }

    /**
     * 规范化JWK（根据RFC 7638）
     * <p>将JWK对象转换为规范化的JSON字符串，用于计算指纹</p>
     * <p>RFC 7638要求：</p>
     * <ul>
     *   <li>按字段名排序（字典序）</li>
     *   <li>无空格</li>
     *   <li>仅包含特定字段（kty, crv, x, y, e, n等，取决于密钥类型）</li>
     *   <li>使用UTF-8编码</li>
     * </ul>
     *
     * <p>参考：<a href="https://www.rfc-editor.org/rfc/rfc7638">RFC 7638 - JSON Web Key (JWK) Thumbprint</a></p>
     *
     * @param jwkObject JWK对象
     * @return 规范化的JSON字符串
     */
    @SuppressWarnings("all")
    private String normalizeJwk(Object jwkObject) {
        try {
            // 1. 将JWK对象转换为Map
            Map<String, Object> jwkMap;
            if (jwkObject instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) jwkObject;
                jwkMap = new HashMap<>(map);
            } else {
                // 如果不是Map，先转换为JSON再解析
                String jwkJson = JsonUtils.toJson(jwkObject);
                jwkMap = JsonUtils.fromJson(jwkJson, Map.class);
            }

            // 2. 提取并过滤字段（根据RFC 7638，只保留特定字段）
            Map<String, Object> normalizedMap = new LinkedHashMap<>();
            String kty = (String) jwkMap.get("kty");
            if (kty == null) {
                throw new IllegalArgumentException("JWK must contain 'kty' field");
            }
            normalizedMap.put("kty", kty);

            // 根据密钥类型提取特定字段
            switch (kty) {
                case "EC" -> {
                    // 椭圆曲线密钥：kty, crv, x, y
                    putIfPresent(normalizedMap, jwkMap, "crv");
                    putIfPresent(normalizedMap, jwkMap, "x");
                    putIfPresent(normalizedMap, jwkMap, "y");
                }
                case "RSA" -> {
                    // RSA密钥：kty, n, e
                    putIfPresent(normalizedMap, jwkMap, "n");
                    putIfPresent(normalizedMap, jwkMap, "e");
                }
                case "oct" ->
                        // 对称密钥：kty, k
                        putIfPresent(normalizedMap, jwkMap, "k");
                case "OKP" -> {
                    // Octet Key Pair（Ed25519等）：kty, crv, x
                    putIfPresent(normalizedMap, jwkMap, "crv");
                    putIfPresent(normalizedMap, jwkMap, "x");
                }
                default -> {
                    log.warn("[Goya] |- security [core] Unsupported JWK key type: {}", kty);
                    // 对于未知类型，保留所有字段（降级策略）
                    normalizedMap.putAll(jwkMap);
                }
            }

            // 3. 按字段名排序（LinkedHashMap已按插入顺序，但我们需要确保排序）
            Map<String, Object> sortedMap = new TreeMap<>(normalizedMap);

            // 4. 转换为无空格的JSON字符串
            String normalizedJson = JsonUtils.toJson(sortedMap);
            // 移除所有空格（RFC 7638要求）
            return normalizedJson.replaceAll("\\s+", "");

        } catch (Exception e) {
            log.error("[Goya] |- security [core] Failed to normalize JWK", e);
            throw new SecurityAuthenticationException("Failed to normalize JWK", e);
        }
    }

    /**
     * 如果字段存在，则添加到目标Map
     *
     * @param target 目标Map
     * @param source 源Map
     * @param key    字段名
     */
    private void putIfPresent(Map<String, Object> target, Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value != null) {
            target.put(key, value);
        }
    }
}

