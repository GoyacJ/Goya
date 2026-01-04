package com.ysmjjsy.goya.security.authentication.provider.sms;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Set;

/**
 * <p>短信验证码Grant Type认证Token</p>
 * <p>包含手机号和短信验证码，用于SMS Grant Type的认证流程</p>
 *
 * <p>继承自OAuth2AuthorizationGrantAuthenticationToken，符合Spring Authorization Server的认证Token规范</p>
 *
 * @author goya
 * @since 2025/12/21
 * @see SmsGrantAuthenticationConverter
 * @see SmsGrantAuthenticationProvider
 */
@Getter
public class SmsGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    /**
     * Grant Type：sms
     */
    public static final AuthorizationGrantType SMS = new AuthorizationGrantType("sms");

    /**
     * 手机号
     */
    private final String phone;

    /**
     * 短信验证码
     */
    private final String smsCode;

    /**
     * 构造函数
     *
     * @param clientId 客户端ID
     * @param phone    手机号
     * @param smsCode  短信验证码
     * @param scopes   请求的scope集合
     */
    public SmsGrantAuthenticationToken(
            String clientId,
            String phone,
            String smsCode,
            String[] scopes) {
        super(SMS, clientId, null, scopes != null ? Set.of(scopes) : null);
        this.phone = phone;
        this.smsCode = smsCode;
    }

    /**
     * 构造函数（带RegisteredClient）
     *
     * @param registeredClient 已注册的客户端
     * @param phone            手机号
     * @param smsCode          短信验证码
     * @param scopes           请求的scope集合
     */
    public SmsGrantAuthenticationToken(
            RegisteredClient registeredClient,
            String phone,
            String smsCode,
            Set<String> scopes) {
        super(SMS, registeredClient, null, scopes);
        this.phone = phone;
        this.smsCode = smsCode;
    }
}

