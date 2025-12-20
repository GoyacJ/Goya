package com.ysmjjsy.goya.security.core.enums;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 13:21
 */
public enum CertificateEnum {
    /**
     * Spring Authorization Server 默认的 JWK 生成方式
     */
    STANDARD,
    /**
     * 自定义证书 JWK 生成方式
     */
    CUSTOM;
}
