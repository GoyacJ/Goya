package com.ysmjjsy.goya.component.security.oauth2.utils;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.StringUtils;

/**
 * <p>DPoP Proof验证器</p>
 * <p>验证客户端提供的DPoP Proof JWT</p>
 * <p>实现RFC 9449规范的DPoP支持</p>
 *
 * <p>参考文档：</p>
 * <ul>
 *   <li><a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/dpop-tokens.html">Spring Security DPoP Documentation</a></li>
 *   <li><a href="https://www.rfc-editor.org/rfc/rfc9449">RFC 9449 - OAuth 2.0 Demonstrating Proof-of-Possession at the Application Layer (DPoP)</a></li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/4 15:45
 */
public class DPoPProofVerifier {

    /**
     * DPoP Proof JWT解码器工厂
     * <p>使用Spring Security官方API进行DPoP Proof验证</p>
     */
    private static final JwtDecoderFactory<DPoPProofContext> dPoPProofVerifierFactory = new DPoPProofJwtDecoderFactory();

    private DPoPProofVerifier() {
    }

    /**
     * 验证DPoP Proof（如果可用）
     * <p>从授权请求的额外参数中提取DPoP Proof并验证</p>
     * <p>验证包括：</p>
     * <ul>
     *   <li>JWT签名验证</li>
     *   <li>htm（HTTP Method）和htu（HTTP URI）验证</li>
     *   <li>iat和jti验证</li>
     * </ul>
     *
     * @param authorizationGrantAuthentication 授权请求认证Token
     * @return 验证后的DPoP Proof JWT，如果不存在则返回null
     * @throws OAuth2AuthenticationException 如果DPoP Proof验证失败
     */
    public static Jwt verifyIfAvailable(OAuth2AuthorizationGrantAuthenticationToken authorizationGrantAuthentication) {
        String dPoPProof = (String) authorizationGrantAuthentication.getAdditionalParameters().get("dpop_proof");
        if (!StringUtils.hasText(dPoPProof)) {
            return null;
        }

        String method = (String) authorizationGrantAuthentication.getAdditionalParameters().get("dpop_method");
        String targetUri = (String) authorizationGrantAuthentication.getAdditionalParameters().get("dpop_target_uri");

        Jwt dPoPProofJwt;
        try {
            // 使用Spring Security官方API构建DPoP Proof验证上下文
            // @formatter:off
            DPoPProofContext dPoPProofContext = DPoPProofContext.withDPoPProof(dPoPProof)
                    .method(method)
                    .targetUri(targetUri)
                    .build();
            // @formatter:on

            // 创建JWT解码器并验证DPoP Proof
            JwtDecoder dPoPProofVerifier = dPoPProofVerifierFactory.createDecoder(dPoPProofContext);
            dPoPProofJwt = dPoPProofVerifier.decode(dPoPProof);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_DPOP_PROOF, "DPoP Proof验证失败: " + ex.getMessage(), null),
                    ex);
        }

        return dPoPProofJwt;
    }
}
