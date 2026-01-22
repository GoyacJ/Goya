package com.ysmjjsy.goya.component.social.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>第三方授权登录的请求参数封装</p>
 *
 * @author goya
 * @since 2026/1/21 09:18
 */
@Data
@Schema(description = "第三方授权登录的请求参数封装")
@NoArgsConstructor
public class ThirdPrincipal implements Serializable {
    @Serial
    private static final long serialVersionUID = -9056240035798420883L;

    @Schema(name = "后回调时带的参数code", title = "访问AuthorizeUrl后回调时带的参数code")
    private String code;
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
}
