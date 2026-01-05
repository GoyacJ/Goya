package com.ysmjjsy.goya.security.resource.server.dpop;

import com.ysmjjsy.goya.security.core.dpop.DPoPKeyFingerprintService;
import com.ysmjjsy.goya.security.resource.server.configuration.properties.SecurityResourceProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

/**
 * <p>资源服务器DPoP验证器</p>
 * <p>验证DPoP-bound Access Token的DPoP Proof</p>
 * <p>实现RFC 9449规范的资源服务器端DPoP验证</p>
 *
 * <p>验证逻辑：</p>
 * <ol>
 *   <li>检查JWT是否包含cnf.jkt claim（表示是DPoP-bound Token）</li>
 *   <li>如果包含，验证请求中是否提供了DPoP Proof</li>
 *   <li>验证DPoP Proof的签名和claims（htm, htu, ath）</li>
 *   <li>验证DPoP Proof的公钥指纹是否与Token中的jkt匹配</li>
 * </ol>
 *
 * <p>参考文档：</p>
 * <ul>
 *   <li><a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/dpop-tokens.html">Spring Security DPoP Documentation</a></li>
 *   <li><a href="https://www.rfc-editor.org/rfc/rfc9449">RFC 9449 - OAuth 2.0 Demonstrating Proof-of-Possession at the Application Layer (DPoP)</a></li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class ResourceServerDPoPValidator {

    private final JwtDecoder dPoPProofDecoder;
    private final SecurityResourceProperties.DPoPConfig dpopConfig;
    private final DPoPKeyFingerprintService dPoPKeyFingerprintService;

    /**
     * 验证DPoP Proof（如果Token是DPoP-bound）
     *
     * @param jwt     JWT Access Token
     * @param request HTTP请求
     * @throws OAuth2AuthenticationException 如果DPoP验证失败
     */
    public void validateIfRequired(Jwt jwt, HttpServletRequest request) {
        // 1. 检查是否启用DPoP验证
        if (!dpopConfig.enabled()) {
            return;
        }

        // 2. 检查JWT是否包含cnf.jkt claim（表示是DPoP-bound Token）
        @SuppressWarnings("unchecked")
        Map<String, Object> cnf = jwt.getClaim("cnf");
        if (cnf == null || !cnf.containsKey("jkt")) {
            // 不是DPoP-bound Token，无需验证
            log.trace("[Goya] |- security [resource] JWT does not contain cnf.jkt claim, skipping DPoP validation");
            return;
        }

        String jkt = (String) cnf.get("jkt");
        log.debug("[Goya] |- security [resource] JWT is DPoP-bound with jkt: {}", jkt);

        // 3. 如果强制要求DPoP，检查请求中是否提供了DPoP Proof
        if (dpopConfig.requireDpopForBoundTokens()) {
            String dPoPProof = request.getHeader("DPoP");
            if (StringUtils.isBlank(dPoPProof)) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(
                                OAuth2ErrorCodes.INVALID_TOKEN,
                                "DPoP-bound Token requires DPoP Proof in DPoP header",
                                null
                        )
                );
            }

            // 4. 验证DPoP Proof
            validateDPoPProof(dPoPProof, jwt, request, jkt);
        }
    }

    /**
     * 验证DPoP Proof
     *
     * @param dPoPProof DPoP Proof JWT字符串
     * @param accessTokenJwt Access Token JWT
     * @param request HTTP请求
     * @param expectedJkt 期望的公钥指纹（从Access Token的cnf.jkt claim获取）
     * @throws OAuth2AuthenticationException 如果验证失败
     */
    private void validateDPoPProof(
            String dPoPProof,
            Jwt accessTokenJwt,
            HttpServletRequest request,
            String expectedJkt) {
        try {
            // 1. 解码DPoP Proof JWT
            Jwt dPoPProofJwt = dPoPProofDecoder.decode(dPoPProof);

            // 2. 验证DPoP Proof的类型
            String typ = dPoPProofJwt.getHeader().get("typ").toString();
            if (!"dpop+jwt".equals(typ)) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(
                                OAuth2ErrorCodes.INVALID_TOKEN,
                                "DPoP Proof must have typ='dpop+jwt'",
                                null
                        )
                );
            }

            // 3. 验证HTTP Method（htm claim）
            String htm = dPoPProofJwt.getClaimAsString("htm");
            String requestMethod = request.getMethod();
            if (!requestMethod.equalsIgnoreCase(htm)) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(
                                OAuth2ErrorCodes.INVALID_TOKEN,
                                String.format("DPoP Proof htm claim (%s) does not match request method (%s)", htm, requestMethod),
                                null
                        )
                );
            }

            // 4. 验证HTTP URI（htu claim）
            String htu = dPoPProofJwt.getClaimAsString("htu");
            String requestUri = buildRequestUri(request);
            if (!requestUri.equals(htu)) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(
                                OAuth2ErrorCodes.INVALID_TOKEN,
                                String.format("DPoP Proof htu claim (%s) does not match request URI (%s)", htu, requestUri),
                                null
                        )
                );
            }

            // 5. 验证Access Token Hash（ath claim）
            String ath = dPoPProofJwt.getClaimAsString("ath");
            if (StringUtils.isNotBlank(ath)) {
                String accessTokenValue = accessTokenJwt.getTokenValue();
                String calculatedAth = calculateAccessTokenHash(accessTokenValue);
                if (!calculatedAth.equals(ath)) {
                    throw new OAuth2AuthenticationException(
                            new OAuth2Error(
                                    OAuth2ErrorCodes.INVALID_DPOP_PROOF,
                                    "DPoP Proof ath claim does not match access token hash",
                                    null
                            )
                    );
                }
            }

            // 6. 验证公钥指纹（jkt）
            String actualJkt = extractJktFromDPoPProof(dPoPProofJwt);
            if (!expectedJkt.equals(actualJkt)) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(
                                OAuth2ErrorCodes.INVALID_TOKEN,
                                String.format("DPoP Proof jkt (%s) does not match access token cnf.jkt (%s)", actualJkt, expectedJkt),
                                null
                        )
                );
            }

            log.debug("[Goya] |- security [resource] DPoP Proof validation successful for jkt: {}", expectedJkt);

        } catch (JwtException e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            OAuth2ErrorCodes.INVALID_DPOP_PROOF,
                            "DPoP Proof validation failed: " + e.getMessage(),
                            null
                    ),
                    e
            );
        }
    }

    /**
     * 从DPoP Proof JWT中提取公钥指纹（jkt）
     *
     * @param dPoPProofJwt DPoP Proof JWT
     * @return 公钥指纹
     */
    private String extractJktFromDPoPProof(Jwt dPoPProofJwt) {
        // 使用认证服务器端的DPoPKeyFingerprintService提取指纹
        String jkt = dPoPKeyFingerprintService.extractFingerprint(dPoPProofJwt);
        if (jkt == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            OAuth2ErrorCodes.INVALID_DPOP_PROOF,
                            "Failed to extract jkt from DPoP Proof",
                            null
                    )
            );
        }
        return jkt;
    }

    /**
     * 计算Access Token的SHA-256哈希（用于验证ath claim）
     *
     * @param accessToken Access Token值
     * @return Base64URL编码的哈希值
     */
    private String calculateAccessTokenHash(String accessToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(accessToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 构建请求URI（用于验证htu claim）
     *
     * @param request HTTP请求
     * @return 完整的请求URI（包含scheme、host、path，不包含query string）
     */
    private String buildRequestUri(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String path = request.getRequestURI();

        StringBuilder uri = new StringBuilder();
        uri.append(scheme).append("://").append(host);
        if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
            uri.append(":").append(port);
        }
        uri.append(path);

        return uri.toString();
    }
}

