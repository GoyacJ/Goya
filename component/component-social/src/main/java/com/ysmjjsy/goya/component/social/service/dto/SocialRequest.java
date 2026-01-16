package com.ysmjjsy.goya.component.social.service.dto;

import com.ysmjjsy.goya.component.core.pojo.DTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/6 09:49
 */
@Data
public class SocialRequest implements DTO {

    @Serial
    private static final long serialVersionUID = 3198218540171641047L;

    /* ---------- 共性参数 ---------- */

    @Schema(name = "后回调时带的参数code", title = "访问AuthorizeUrl后回调时带的参数code")
    private String code;

    /* ---------- 微信小程序常用参数 ---------- */

    @Schema(name = "小程序appId", title = "小程序appId")
    private String appId;

    @Schema(name = "消息密文", title = "微信小程序消息密文")
    private String encryptedData;

    @Schema(name = "加密算法的初始向量", title = "微信小程序加密算法的初始向量")
    private String iv;

    @Schema(name = "小程序用户openId", title = "小程序用户openId")
    private String openId;

    @Schema(name = "会话密钥", title = "微信小程序会话密钥")
    private String sessionKey;

    @Schema(name = "唯一ID", title = "微信唯一ID")
    private String unionId;

    @Schema(name = "用户非敏感信息", title = "微信小程序用户非敏感信息")
    private String rawData;

    @Schema(name = "签名", title = "微信小程序签名")
    private String signature;

    /* ---------- Just Auth 标准参数 ---------- */

    @Schema(name = "后回调时带的参数auth_code", title = "该参数目前只使用于支付宝登录")
    private String auth_code;

    @Schema(name = "后回调时带的参数state", title = "用于和请求AuthorizeUrl前的state比较，防止CSRF攻击")
    private String state;

    @Schema(name = "华为授权登录接受code的参数名")
    private String authorization_code;

    @Schema(name = "回调后返回的oauth_token", title = "Twitter回调后返回的oauth_token")
    private String oauth_token;

    @Schema(name = "回调后返回的oauth_verifier", title = "Twitter回调后返回的oauth_verifier")
    private String oauth_verifier;

    /* ---------- 手机短信验证码 ---------- */

    @Schema(name = "手机号码", title = "手机短信登录唯一标识")
    private String mobile;
}
